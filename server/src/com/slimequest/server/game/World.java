package com.slimequest.server.game;

import com.slimequest.server.Game;
import com.slimequest.server.RunInWorld;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

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
    private HashMap<String, GameObject> objects = new HashMap<String, GameObject>();

    // Things from other threads that are pending
    private final Stack<RunInWorld> pendingPosts = new Stack<RunInWorld>();

    @Override
    public void getEvent(GameNetworkEvent event) {

        // Get associated object id
        String id;
        GameObject object;

        if (GameEvent.REMOVE_OBJECT.equals(event.getType())) {
            remove(EventAttr.getId(event));

            return;
        } else if (GameEvent.CREATE_OBJECT.equals(event.getType())) {
            MapObject obj = new Teleport();
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
                    pendingPosts.pop().runInWorld(this);
                }
            }
        }
    }

    public GameObject get(String id) {
        return objects.get(id);
    }

    public void add(GameObject object) {

        // Add to world
        objects.put(object.id, object);

        // Add to map
        if (MapObject.class.isAssignableFrom(object.getClass())) {
            // Identify self with client if necessary
            object.getEvent(new GameNetworkEvent(GameEvent.IDENTIFY, objJson((MapObject) object)));

            Map map = ((MapObject) object).map;

            if (map != null) {
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

    public void move(MapObject object, Map map, int x, int y) {
        if (MapObject.class.isAssignableFrom(object.getClass())) {
            // Remove the object from th map
            if (object.map != null) {
                object.map.remove(object.id);
            }

            // Move the object
            object.map = map;
            object.x = x;
            object.y = y;

            // Add the object to the new map
            if (map != null) {
                map.add(object);
            }
        }
    }

    public void post(RunInWorld runInWorld) {
        pendingPosts.push(runInWorld);
    }
}
