package com.slimequest.game.game;

import com.badlogic.gdx.math.Vector2;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

import java.util.HashMap;

/**
 * Created by jacob on 9/11/16.
 */

public class Map extends GameObject {
    private final java.util.Map<String, MapObject> mapObjects = new HashMap<String, MapObject>();
    private final java.util.Map<Vector2, MapTile> mapTiles = new HashMap<Vector2, MapTile>();

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.MOVE.equals(event.getType())) {
            String id = EventAttr.getId(event);
            int x = EventAttr.getX(event);
            int y = EventAttr.getY(event);

            if (!mapObjects.containsKey(id)) {
                return;
            }

            MapObject mapObject = mapObjects.get(id);
            mapObject.moveTo(x, y);
        }

        else if (GameEvent.MAP_TILES.equals(event.getType())) {
            // XXX TODO get map tiles
        }
    }

    @Override
    public void update() {
        for (MapObject mapObject : mapObjects.values()) {
            mapObject.update();
        }
    }

    @Override
    public void render() {
        // Render self
        // Get game view
        // Check for tiles at all those location
        // Draw any found tiles

        // XXX Concurrent modification thrown here when new objects are added / removed
        for (MapObject mapObject : mapObjects.values()) {
            mapObject.render();
        }

        // Render upper tile layer

        // Render rain effects
    }

    public void add(MapObject mapObject) {
        mapObjects.put(mapObject.id, mapObject);
    }

    public void remove(String id) {
        if (!mapObjects.containsKey(id)) {
            return;
        }

        mapObjects.remove(id);
    }
}
