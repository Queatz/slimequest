package com.slimequest.server.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

import java.awt.Point;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import static com.slimequest.server.ServerHandler.objJson;

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
    // Format: [x, y, t, g]
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
        evt.add("id", new JsonPrimitive(id));
        evt.add("tiles", getMapTilesJson());
        return evt;
    }

    // Set map tiles from JSON
    // Format: [x, y, t, g]
    private void setMapTilesFromJson(JsonArray tiles) {

        for (JsonElement tile : tiles) {
            int x = tile.getAsJsonArray().get(0).getAsInt();
            int y = tile.getAsJsonArray().get(1).getAsInt();
            int t = tile.getAsJsonArray().get(2).getAsInt();
            mapTiles.put(new Point(x, y), new MapTile(t));
        }
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.EDIT_TILE.equals(event.getType())) {
            // XXX Authorize is map admin of this map or map group

            JsonArray tupdate = event.getData().getAsJsonObject().getAsJsonArray("tile");

            Point tp = new Point(
                    tupdate.get(0).getAsInt(),
                    tupdate.get(1).getAsInt()
            );
            int tt = tupdate.get(2).getAsInt();

            if (tt == -1) {
                mapTiles.remove(tp);
            } else {
                mapTiles.put(tp, new MapTile(tt));
            }

            // Turn edit event into new tile event
            JsonObject evt = new JsonObject();
            JsonArray tiles = new JsonArray();
            tiles.add(tupdate);
            evt.add("id", new JsonPrimitive(id));
            evt.add("tiles", tiles);
            event = new GameNetworkEvent(GameEvent.MAP_TILES, evt);
        }

        // Maps just propagate events...
        for (MapObject object : mapObjects.values()) {
            object.getEvent(event);
        }
    }

    public Collection<MapObject> find() {
        return mapObjects.values();
    }

    public void add(MapObject mapObject) {
        // Add object to map
        mapObjects.put(mapObject.id, mapObject);

        // Notify map's objects of the object leaving
        getEvent(new GameNetworkEvent(GameEvent.JOIN, objJson(mapObject)));

        // Simulate all existing objects joining map for the newcomer
        for (MapObject object : mapObjects.values()) {
            mapObject.getEvent(new GameNetworkEvent(GameEvent.JOIN, objJson(object)));
        }

        // Send map tiles
        mapObject.getEvent(new GameNetworkEvent(GameEvent.MAP_TILES, getMapTilesJsonEvt()));
    }

    public void remove(String id) {
        if (!mapObjects.containsKey(id)) {
            return;
        }

        // Remove object from map
        mapObjects.remove(id);

        // Notify map's objects of the object leaving
        getEvent(new GameNetworkEvent(GameEvent.LEAVE, new JsonPrimitive(id)));
    }
}
