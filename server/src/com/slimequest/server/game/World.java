package com.slimequest.server.game;

import com.google.gson.JsonPrimitive;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

import java.util.HashMap;

import static com.slimequest.server.ServerHandler.playerJson;

/**
 * Created by jacob on 9/11/16.
 */

public class World extends GameObject {
    private HashMap<String, Map> maps = new HashMap<String, Map>();
    Map activeMap;

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (activeMap != null) {
            activeMap.getEvent(event);
        }
    }

    public void add(GameObject player) {
        if (activeMap == null) {
            activeMap = new Map();
            maps.put("default", activeMap); // XXX lol todo
        }

        player.getEvent(new GameNetworkEvent(GameEvent.IDENTIFY, playerJson((Player) player)));

        if (MapObject.class.isAssignableFrom(player.getClass())) {
            activeMap.add((MapObject) player);
            activeMap.getEvent(new GameNetworkEvent(GameEvent.JOIN, playerJson((MapObject) player)));
        } else {
            // What to do with this object...
        }
    }

    public void remove(String id) {
        if (activeMap != null) {
            activeMap.remove(id);

            activeMap.getEvent(new GameNetworkEvent(GameEvent.LEAVE, new JsonPrimitive(id)));
        }
    }
}
