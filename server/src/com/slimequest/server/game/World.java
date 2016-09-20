package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.Game;
import com.slimequest.server.RunInWorld;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;
import com.slimequest.shared.Json;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import static com.slimequest.server.ServerHandler.objJson;

/**
 * Created by jacob on 9/11/16.
 */

public class World extends GameObject {
    @Override
    public String getType() {
        return GameType.WORLD;
    }

    // All the objects in the world
    private HashMap<String, GameObject> objects = new HashMap<>();

    // Things from other threads that are pending
    private final ConcurrentLinkedQueue<RunInWorld> pendingPosts = new ConcurrentLinkedQueue<>();

    @Override
    public void getEvent(GameNetworkEvent event) {

        // Get associated object id
        String id;
        GameObject object;

        if (GameEvent.REMOVE_OBJECT.equals(event.getType())) {
            remove(EventAttr.getId(event));

            return;
        } else if (GameEvent.CREATE_OBJECT.equals(event.getType())) {
            MapObject obj = new Teleport(); // XXX TODO Not just teleport!
            obj.id = EventAttr.getId(event);
            obj.map = (Map) get(EventAttr.getMapId(event));
            obj.x = EventAttr.getX(event);
            obj.y = EventAttr.getY(event);

            add(obj);

            return;
        } else if (GameEvent.EDIT_TELEPORT_TARGET.equals(event.getType())) {
            id = EventAttr.getId(event);
            object = get(id);

            if (object != null) {
                Teleport teleport = (Teleport) object;
                teleport.target = event.getData().getAsJsonObject().get(GameAttr.DATA).getAsString();
            }

            // Do not forward event
            return;
        } else {
            id = EventAttr.getId(event);
            object = get(id);
        }

        // Forward event to map of object
        // XXX May want to change to direct object
        if (GameType.MAP.equals(object.getType())) {
            object.getEvent(event);
        } else if (MapObject.class.isAssignableFrom(object.getClass())) {
            if (((MapObject) object).map != null) {
                ((MapObject) object).map.getEvent(event);
            }
        }
    }

    @Override
    public void update(float delta) {
        // XXX Only update maps with user activity
        for (GameObject object : objects.values()) {
            object.update(delta);
        }

        if (!pendingPosts.isEmpty()) {
            synchronized (pendingPosts) {
                while (!pendingPosts.isEmpty()) {
                    pendingPosts.poll().runInWorld(this);
                }
            }
        }
    }

    public <T extends GameObject> T getOrCreate(Class<T> clazz, String id) {
        T object = (T) get(id, true);

        if (object == null) {
            try {
                object = clazz.newInstance();
                object.id = id;
                add(object);
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        return object;
    }

    public GameObject get(String id) {
        return get(id, true);
    }

    public GameObject get(String id, boolean load) {
        GameObject gameObject =  objects.get(id);

        if (load && gameObject == null) {
            Game.openDb();
            String string = Game.fossils.get(id);
            Game.closeDb();

            if (string != null) {
                gameObject = (GameObject) Fossilize.defossilize(Json.from(string, JsonObject.class));
                add(gameObject);
            }
        }

        return gameObject;
    }

    public void add(GameObject object) {
        if (object == null) {
            return;
        }

        // Add to world
        objects.put(object.id, object);

        // Add to map
        if (MapObject.class.isAssignableFrom(object.getClass())) {
            Map map = ((MapObject) object).map;

            if (map != null) {
                // Identify self with client if necessary
                object.getEvent(new GameNetworkEvent(GameEvent.IDENTIFY, objJson((MapObject) object)));

                map.add((MapObject) object);
            }
        } else {
            // What to do with this object...
        }
    }

    public void remove(String id) {
        GameObject object = get(id);

        if (object == null) {
            return;
        }

        // Remove from map
        if (MapObject.class.isAssignableFrom(object.getClass())) {
            Map map = ((MapObject) object).map;

            if (map != null) {
                map.remove(id);
            }
        }

        // Remove from world
        objects.remove(id);
    }

    public void move(MapObject object, int newX, int newY) {
        move(object, object.map, newX, newY);
    }

    public void move(MapObject object, Map map, int x, int y) {
        boolean movesAcrossMaps = object.map != map;

        if (MapObject.class.isAssignableFrom(object.getClass())) {
            // Remove the object from th map
            if (object.map != null && movesAcrossMaps) {
                object.map.remove(object.id);
            }

            // Move the object
            object.map = map;
            object.x = x;
            object.y = y;

            // Add the object to the new map
            if (map != null && movesAcrossMaps) {
                map.add(object);
            }

            if (object.map != null && !movesAcrossMaps) {
                GameNetworkEvent event = new GameNetworkEvent(GameEvent.MOVE, objJson(object));

                // Ensure it gets sent to its own client
                if (GameType.PLAYER.equals(object.getType())) {
                    event.getData().getAsJsonObject().add(GameAttr.IMPORTANT, new JsonPrimitive(true));
                }

                map.getEvent(event);
            }
        }
    }

    public void post(RunInWorld runInWorld) {
        pendingPosts.add(runInWorld);
    }

    public void load(ConcurrentMap<String, String> fossils) {
        for (String slimeObj : fossils.values()) {
            GameObject gameObject = (GameObject) Fossilize.defossilize(Json.from(slimeObj, JsonObject.class));
//            add(gameObject); // See GameObject.java

            if (gameObject == null) {
                continue;
            }

            System.out.println("Added " + gameObject.getType() + "...");
        }
    }

    public void save(ConcurrentMap<String, String> fossils) {
        // Make sure the database is cleared
        fossils.clear();

        for (GameObject gameObject : objects.values()) {
            JsonObject fossil = Fossilize.fossilize(gameObject);

            if (fossil == null) {
                System.out.println("Skipping save of " + gameObject.getType() + "...");
                continue;
            }

            fossils.put(fossil.get(GameAttr.ID).getAsString(), Json.to(fossil));

            System.out.println("Saved " + gameObject.getType() + "...");
        }
    }
}
