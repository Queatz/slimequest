package com.slimequest.game.game;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

import java.util.Date;
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

    // The player who is currently it
    public String itPlayerId;

    // Last time a sound was played when an object was added
    private Date lastAdded;

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

    public void clear() {
        Game.player = null;
        Game.playerId = null;
        objects.clear();
        activeMap = null;
        lastAdded = null;
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.GAME_STATE.equals(event.getType())) {

            // Set the currently it player
            if (event.getData().getAsJsonObject().has("itPlayer")) {
                itPlayerId = event.getData().getAsJsonObject().get("itPlayer").getAsString();
            } else {
                itPlayerId = null;
            }
        }

        else if (GameEvent.IDENTIFY.equals(event.getType())) {

            // Identify the player
            Game.player = (Player) create(GameType.PLAYER);
            Game.player.id = EventAttr.getId(event);
            Game.player.pos.x = EventAttr.getX(event);
            Game.player.pos.y = EventAttr.getY(event);
            Game.player.map = get(Map.class, EventAttr.getMapId(event));
            Game.playerId = Game.player.id;

            // Set the active map to the map the player is in
            activeMap = Game.player.map;

            // IDENTIFIED!!!! START THE MUSIC!!!!
            Music music = GameResources.mus("bunny3.ogg");
            music.setLooping(true);
            music.play();

            // Add player to world
            add(Game.player);
        }

        else if (GameEvent.MOVE.equals(event.getType())) {

            // Find associated object and map
            final GameObject object = get(EventAttr.getId(event));
            Map map = Map.getMapOf(object);

            // Forward move event to map
            if (map != null) {
                map.getEvent(event);
            }
        }

        else if (GameEvent.OBJECT_STATE.equals(event.getType())) {

            // Get existing object
            String id = EventAttr.getId(event);
            GameObject object = get(id);

            if (object == null) {
                return;
            }

            // Update frozen state of object
            if (Player.class.isAssignableFrom(object.getClass())) {
                if (event.getData().getAsJsonObject().has(GameAttr.FROZEN)) {
                    ((Player) object).frozen = event.getData().getAsJsonObject()
                        .get(GameAttr.FROZEN).getAsBoolean();
                }
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

            // XXX todo need this still?
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
                ((MapObject) object).initialPos(new Vector2(x, y));
            }

            // In case the player moves maps, re-associate
            if (Game.playerId.equals(object.id)) {
                activeMap = ((MapObject) object).map;
                Game.player = (Player) object;
                Game.playerId = object.id;
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
        if (lastAdded == null || new Date().after(new Date(lastAdded.getTime() + 250))) {
            GameResources.snd("teleport.ogg").play();
            lastAdded = new Date();
        }

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
