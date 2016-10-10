package com.slimequest.server.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.Game;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;
import com.slimequest.shared.MapTiles;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;

import static com.slimequest.server.Game.objJson;

/**
 * Created by jacob on 9/11/16.
 */

public class Map extends GameObject {

    @Override
    public JsonObject fossilize() {
        JsonObject fossil = super.fossilize();

        fossil.add("tiles", getMapTilesJson());

        return fossil;
    }

    @Override
    public void defossilize(JsonObject fossil) {
        super.defossilize(fossil);

        setMapTilesFromJson(fossil.get("tiles").getAsJsonArray());
    }

    @Override
    public String getType() {
        return GameType.MAP;
    }

    // All the objects in this map
    private final java.util.Map<String, MapObject> mapObjects = new HashMap<>();

    // The map tiles
    private final java.util.Map<Point, MapTile> mapTiles = new HashMap<>();

    // Get all map tiles as JSON
    // Format: [x, y, t]
    private JsonElement getMapTilesJson() {
        JsonArray tiles = new JsonArray();

        for (java.util.Map.Entry<Point, MapTile> tile : mapTiles.entrySet()) {
            JsonArray t = new JsonArray();
            t.add(tile.getKey().x);
            t.add(tile.getKey().y);
            t.add(tile.getValue().type);
            t.add(tile.getValue().group);
            tiles.add(t);
        }

        return tiles;
    }

    private JsonElement getMapTilesJsonEvt() {
        JsonObject evt = new JsonObject();
        evt.add(GameAttr.ID, new JsonPrimitive(id));
        evt.add(GameAttr.TILES, getMapTilesJson());
        return evt;
    }

    // Set map tiles from JSON
    // Format: [x, y, t]
    private void setMapTilesFromJson(JsonArray tiles) {
        for (JsonElement tile : tiles) {
            int x = tile.getAsJsonArray().get(0).getAsInt();
            int y = tile.getAsJsonArray().get(1).getAsInt();
            int t = tile.getAsJsonArray().get(2).getAsInt();

            int g = 0;

            if (tile.getAsJsonArray().size() > 3) {
                g = tile.getAsJsonArray().get(3).getAsInt();
            }

            mapTiles.put(new Point(x, y), new MapTile(t, g));
        }
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.EDIT_TILE.equals(event.getType())) {
            // XXX todo Authorize is map admin of this map or map group

            JsonArray tupdate = EventAttr.getTile(event);

            Point tp = new Point(
                    tupdate.get(0).getAsInt(),
                    tupdate.get(1).getAsInt()
            );

            int tt = tupdate.get(2).getAsInt();

            int g = 0;

            if (tupdate.size() > 3) {
                g = tupdate.get(3).getAsInt();
            }

            if (tt == -1) {
                mapTiles.remove(tp);
            } else {
                mapTiles.put(tp, new MapTile(tt, g));
            }

            JsonObject evt = new JsonObject();
            JsonArray tiles = new JsonArray();

            // Turn edit event into new tile event and propagate
            tiles.add(tupdate);
            evt.add(GameAttr.ID, new JsonPrimitive(id));
            evt.add(GameAttr.TILES, tiles);
            event = new GameNetworkEvent(GameEvent.MAP_TILES, evt);
        }

        // Maps are just ultimate event propagators...
        for (MapObject object : mapObjects.values()) {
            object.getEvent(event);
        }
    }

    // Find all map objects
    public Collection<MapObject> find() {
        return mapObjects.values();
    }

    // Add object to map
    public void add(MapObject mapObject) {
        mapObjects.put(mapObject.id, mapObject);

        // Notify map's objects of the object leaving
        getEvent(new GameNetworkEvent(GameEvent.JOIN, objJson(mapObject)));

        // Simulate all existing objects joining map for the newcomer
        for (MapObject object : mapObjects.values()) {
            mapObject.getEvent(new GameNetworkEvent(GameEvent.JOIN, objJson(object)));
        }

        // Send map tiles to new object
        mapObject.getEvent(new GameNetworkEvent(GameEvent.MAP_TILES, getMapTilesJsonEvt()));
    }

    // Remove object from map
    public void remove(String id) {
        if (!mapObjects.containsKey(id)) {
            return;
        }

        // Remove object from map
        mapObjects.remove(id);

        // Notify map's objects of the object leaving
        getEvent(new GameNetworkEvent(GameEvent.LEAVE, new JsonPrimitive(id)));
    }

    // Check if a point collides with this map
    public boolean checkCollision(Point pos) {
        MapTile mapTile = tileBelow(pos);

        return mapTile == null || MapTiles.collideTiles.get(mapTile.group).contains(mapTile.type);
    }

    // Find the tile below a point
    public MapTile tileBelow(Point pos) {
        pos = new Point((int) Math.floor(pos.x / Game.ts), (int) Math.floor(pos.y / Game.ts));

        if (mapTiles.containsKey(pos)) {
            return mapTiles.get(pos);
        }

        return null;
    }

    // Get all map objects
    public Collection<MapObject> getObjects() {
        return mapObjects.values();
    }
}
