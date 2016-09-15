package com.slimequest.game.game;

import com.badlogic.gdx.math.Vector2;
import com.slimequest.game.Game;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by jacob on 9/11/16.
 */

public class World extends GameObject {

    // All the objects in the world
    private HashMap<String, GameObject> objects = new HashMap<>();

    // The current map the player is on
    public Map activeMap;

    @Override
    public void update() {
        if (activeMap != null) {
            activeMap.update();
        }
    }

    @Override
    public void render() {
        if (activeMap != null) {
            activeMap.render();
        }
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.IDENTIFY.equals(event.getType())) {

            // Identify player
            Game.player = (Player) create(GameType.PLAYER);
            Game.player.id = EventAttr.getId(event);
            Game.player.pos.x = EventAttr.getX(event);
            Game.player.pos.y = EventAttr.getY(event);
            Game.player.map = get(Map.class, EventAttr.getMapId(event));
            Game.playerId = Game.player.id;

            // Set the active map to the map the player is in
            activeMap = Game.player.map;

            // Add player to world
            add(Game.player);
        } else if (GameEvent.MOVE.equals(event.getType())) {

            // Find associated object and map
            final GameObject object = get(EventAttr.getId(event));
            final Map map;
            if (object != null && Map.class.isAssignableFrom(object.getClass())) {
                map = (Map) object;
            } else if (object != null && MapObject.class.isAssignableFrom(object.getClass())) {
                map = ((MapObject) object).map;
            } else {
                map = null;
            }

            // Forward move event to map
            if (map != null) {
                map.getEvent(event);
            }
        }

        else if (GameEvent.JOIN.equals(event.getType())) {

            // Extract event data
            String id = EventAttr.getId(event);
            String mapId = EventAttr.getMapId(event);
            int x = EventAttr.getX(event);
            int y = EventAttr.getY(event);

            // Get existing object
            GameObject object = get(id);

            boolean needsAdd = object == null;

            if (!needsAdd) {
                remove(object.id);
                needsAdd = true;
            }

            if (needsAdd) {
                // Create object
                object = create(EventAttr.getType(event));
                object.id = id;
            }

            // If object belongs to a map, associate it
            if (mapId != null) {
                ((MapObject) object).map = get(Map.class, mapId);
                ((MapObject) object).setPos(new Vector2(x, y));
            }

            // In case the player moves maps, re-associate
            if (Game.playerId.equals(object.id)) {
                activeMap = ((MapObject) object).map;
                Game.player = (Player) object;
            }

            if (needsAdd) {
                add(object);
            }
        }

        else if (GameEvent.LEAVE.equals(event.getType())) {

            // Safe to always just remove the object
            remove(event.getData().getAsString());
        } else if (GameEvent.MAP_TILES.equals(event.getType())) {
            String id = EventAttr.getId(event);
            Map map = get(Map.class, id);
            map.getEvent(event);
        }
    }

    public GameObject create(String type) {
        GameObject object;

        switch (type) {
            case GameType.PLAYER:
                object = new Player();
                break;
            case GameType.SLIME:
                object = new Slime();
                break;
            case GameType.TELEPORT:
                object = new Teleport();
                break;
            default:
                Logger.getAnonymousLogger().warning("Tried to create unknown object type: " + type);
                return null;
        }

        object.id = Long.toHexString(new Random().nextLong());
        return object;
    }

    public void add(GameObject object) {

        // Add to world
        objects.put(object.id, object);

        // Add to map
        if (MapObject.class.isAssignableFrom(object.getClass())) {
            Map map = ((MapObject) object).map;
            if (map != null) {
                map.add((MapObject) object);
            }
        }
    }

    public void remove(String id) {

        // Remove from map
        MapObject object = get(id);
        if (object != null && object.map != null) {
            object.map.remove(id);
        }

        // Remove from world
        objects.remove(id);
    }

    public <T extends GameObject> T get(String id) {
        if (!objects.containsKey(id)) {
            return null;
        }

        return (T) objects.get(id);
    }

    public <T extends GameObject> T get(Class<T> clazz, String id) {
        GameObject object = get(id);

        if (object == null) try {
            T newObject = clazz.newInstance();
            newObject.id = id;
            add(newObject);
            return newObject;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }

        if (clazz.isAssignableFrom(object.getClass())) {
            return (T) object;
        } else {
            Logger.getAnonymousLogger().warning("Tried to get object with invalid type: " +
                    clazz + " was: " + object.getClass());

            return null;
        }
    }
}
