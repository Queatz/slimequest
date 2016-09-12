package com.slimequest.server.game;

import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

import java.util.HashMap;

import static com.slimequest.server.ServerHandler.playerJson;

/**
 * Created by jacob on 9/11/16.
 */

public class Map extends GameObject {
    private final java.util.Map<String, MapObject> mapObjects = new HashMap<String, MapObject>();
//    private final java.util.Map<Vector2, MapTile> mapTiles = new HashMap<Vector2, MapTile>();


    @Override
    public void getEvent(GameNetworkEvent event) {
        // Maps just propagate events...
        for (MapObject object : mapObjects.values()) {
            object.getEvent(event);
        }
    }

    public void add(MapObject mapObject) {
        // Simulate all existing objects joining map
        for (MapObject object : mapObjects.values()) {
            mapObject.getEvent(new GameNetworkEvent(GameEvent.JOIN, playerJson(object)));
        }

        mapObjects.put(mapObject.id, mapObject);
    }

    public void remove(String id) {
        if (!mapObjects.containsKey(id)) {
            return;
        }

        mapObjects.remove(id);
    }
}
