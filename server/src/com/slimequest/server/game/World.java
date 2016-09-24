package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.Game;
import com.slimequest.server.RunInWorld;
import com.slimequest.server.events.GameNotificationEvent;
import com.slimequest.server.events.GameStateEvent;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;
import com.slimequest.shared.Json;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import io.netty.util.internal.ConcurrentSet;

import static com.slimequest.server.Game.objJson;

/**
 * Created by jacob on 9/11/16.
 */

public class World extends GameObject {
    private GameState gameState = new GameState();

    private Date lastItPlayerInteraction = new Date();

    @Override
    public String getType() {
        return GameType.WORLD;
    }

    // All the objects in the world
    private HashMap<String, GameObject> objects = new HashMap<>();

    // Things from other threads that are pending
    private final ConcurrentLinkedQueue<RunInWorld> pendingPosts = new ConcurrentLinkedQueue<>();

    // Things from other threads that are pending
    private final ConcurrentSet<RunInWorld> scheduledPosts = new ConcurrentSet<>();

    @Override
    public void getEvent(GameNetworkEvent event) {

        // Get associated object id
        String id = EventAttr.getId(event);
        GameObject object;

        if (id != null && id.equals(gameState.itPlayer)) {
            lastItPlayerInteraction = new Date();
        }

        if (GameEvent.TAG_PLAYER.equals(event.getType())) {
            String playerId = EventAttr.getId(event);
            GameObject player = Game.world.get(playerId);

            if (player != null) {
                player.getEvent(event);
            }

            return;
        } else if (GameEvent.EDIT_TELEPORT_TARGET.equals(event.getType())) {
            object = get(id);

            if (object != null) {
                object.getEvent(event);
            }

            return;
        } if (GameEvent.REMOVE_OBJECT.equals(event.getType())) {
            remove(EventAttr.getId(event));

            return;
        } else if (GameEvent.CREATE_OBJECT.equals(event.getType())) {
            add((MapObject) Fossilize.defossilize(event.getData().getAsJsonObject()));

            return;
        } else if (GameEvent.GAME_NOTIFICATION.equals(event.getType())) {
            for (GameObject gameObject : objects.values()) {
                if (MapObject.class.isAssignableFrom(gameObject.getClass())) {
                    gameObject.getEvent(event);
                }
            }

            return;
        } else if (GameEvent.GAME_STATE.equals(event.getType())) {
            for (GameObject gameObject : objects.values()) {
                if (MapObject.class.isAssignableFrom(gameObject.getClass())) {
                    gameObject.getEvent(event);
                }
            }

            return;
        } else {
            object = get(id);
        }

        if (object == null) {
            return;
        }

        // Forward event to map of object
        // XXX May want to change to direct object first, then map, and disallow circular reference
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
        // Run pending posts to the world
        Set<RunInWorld> ran = new HashSet<>();

        Date now = new Date();

        for (RunInWorld runInWorld : scheduledPosts) {
            if (runInWorld.scheduled.before(now)) {
                ran.add(runInWorld);
                pendingPosts.add(runInWorld);
            }
        }

        for (RunInWorld runInWorld : ran) {
            scheduledPosts.remove(runInWorld);
        }

        if (!pendingPosts.isEmpty()) {
            synchronized (pendingPosts) {
                while (!pendingPosts.isEmpty()) {
                    pendingPosts.poll().runInWorld(this);
                }
            }
        }

        // Expire after 1 minute of no activity
        if (gameState.itPlayer != null &&
                lastItPlayerInteraction.before(new Date(new Date().getTime() - 1000 * 60))) {
            resetGame();
        }

        // Update game objects
        for (GameObject object : objects.values()) {
            object.update(delta);
        }
    }

    // Get an object, create it if it doesn't exist
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

    // Get object by id
    public GameObject get(String id) {
        return get(id, true);
    }

    // Get an object by id, and load from fossils
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

    // Add an object to the world
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
                // Identify self with client if necessary if it's a map object
                object.getEvent(new GameNetworkEvent(GameEvent.IDENTIFY, objJson((MapObject) object)));

                map.add((MapObject) object);
            }
        } else {
            // What to do with this object...
        }
    }

    // Remove an object from the world
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

        // If the player who is it leaves, notify all clients
        if (id.equals(gameState.itPlayer)) {
           resetGame();
        }
    }

    private void resetGame() {
        setWhosIt(null);

        // Tell them to get the butterfly
        getEvent(new GameNotificationEvent(":butterfly", "find the\nbutterfly!"));

        // Unfreeze everybody
        for (GameObject gameObject : objects.values()) {
            if (Player.class.isAssignableFrom(gameObject.getClass()) && ((Player) gameObject).frozen) {
                ((Player) gameObject).frozen = false;
                ((Player) gameObject).map.getEvent(new GameNetworkEvent(GameEvent.OBJECT_STATE, Game.objJson((Player) gameObject)));
            }
        }
    }

    public void move(MapObject object, int newX, int newY) {
        move(object, object.map, newX, newY, false);
    }

    // Move an object in the world, possibly across maps
    public void move(MapObject object, Map map, int x, int y, boolean teleport) {
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

            // If it's a move within the map, send a move event
            if (object.map != null && !movesAcrossMaps) {
                GameNetworkEvent event = new GameNetworkEvent(GameEvent.MOVE, objJson(object));

                // Ensure that it gets sent to its own client
                if (teleport) {
                    event.getData().getAsJsonObject().add(GameAttr.TELEPORT, new JsonPrimitive(true));
                }

                map.getEvent(event);
            }
        }
    }

    public void post(RunInWorld runInWorld, int delay) {
        if (delay > 0) {
            runInWorld.scheduled = new Date(new Date().getTime() + delay);
            scheduledPosts.add(runInWorld);
        } else {
            post(runInWorld);
        }
    }

    // Run something in the next game loop cycle
    // This is thread safe and can be called from anywhere
    public void post(RunInWorld runInWorld) {
        pendingPosts.add(runInWorld);
    }

    // Defossilize the world
    public void load(ConcurrentMap<String, String> fossils) {
        for (String slimeObj : fossils.values()) {
            GameObject gameObject = (GameObject) Fossilize.defossilize(Json.from(slimeObj, JsonObject.class));
            // add(gameObject); // See GameObject.java

            if (gameObject == null) {
                continue;
            }

            System.out.println("Added " + gameObject.getType() + "...");
        }
    }

    // Fossilize the world
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

    // Get who's it
    public String whosIt() {
        return gameState.itPlayer;
    }

    // Get all the objects in the world
    public HashMap<String, GameObject> getObjects() {
        return objects;
    }

    public void setWhosIt(String whosIt) {
        gameState.itPlayer = whosIt;
        lastItPlayerInteraction = new Date();
        getEvent(new GameStateEvent(whosIt));
    }
}
