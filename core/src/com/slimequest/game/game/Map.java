package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;
import com.slimequest.game.events.GameNetworkEditTileEvent;
import com.slimequest.shared.EventAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 9/11/16.
 */

public class Map extends GameObject {
    private final java.util.Map<String, MapObject> mapObjects = new HashMap<>();
    private final java.util.Map<Vector2, MapTile> mapTiles = new HashMap<>();

    private final static Set<Integer> collideTiles = new HashSet<>();

    static {
        // TODO defined by tileset specification...
        // Needs support for groups
        collideTiles.add(1000);
        collideTiles.add(2000);
        collideTiles.add(3000);
        collideTiles.add(1);
        collideTiles.add(1001);
        collideTiles.add(3001);
        collideTiles.add(2);
        collideTiles.add(1002);
        collideTiles.add(2002);
        collideTiles.add(3002);
        collideTiles.add(2003);
        collideTiles.add(3003);
        collideTiles.add(2004);
        collideTiles.add(3004);
    }

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
            mapObject.moveTo(new Vector2(x, y));
        }

        else if (GameEvent.MAP_TILES.equals(event.getType())) {
            String mapId = EventAttr.getId(event);

            // Discard the tiles if they aren't for the current map
            if (!id.equals(mapId)) {
                return;
            }

            JsonArray tiles = EventAttr.getTiles(event);

            // Read tiles into map
            if (tiles.size() > 0) {
                for (JsonElement t : tiles) {
                    JsonArray tile = t.getAsJsonArray();

                    int x = tile.get(0).getAsInt();
                    int y = tile.get(1).getAsInt();
                    int type = tile.get(2).getAsInt();

                    if (type == -1) {
                        mapTiles.remove(new Vector2(x, y));
                    } else {
                        mapTiles.put(new Vector2(x, y), new MapTile(type));
                    }
                }
            }
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

        Texture tilesImage = GameResources.img("grassy_tiles.png");

        // Get the visible tile ranges
        int ts = Game.ts;
        int minX = (int) Math.floor((Game.viewportXY.x - Game.viewportSize / 2) / ts);
        int minY = (int) Math.floor((Game.viewportXY.y - Game.viewportSize / 2) / ts);
        int maxX = (int) Math.ceil((Game.viewportXY.x + Game.viewportSize / 2) / ts);
        int maxY = (int) Math.ceil((Game.viewportXY.y + Game.viewportSize / 2) / ts);

        // Draw map tiles
        Vector2 vec = new Vector2();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                vec.set(x, y);

                // Draw the tile at the location if there is one
                if (mapTiles.containsKey(vec)) {
                    MapTile tile = mapTiles.get(vec);

                    TextureRegion region = new TextureRegion(tilesImage,
                            MapTile.getX(tile) * ts,
                            MapTile.getY(tile) * ts,
                            ts, ts);

                    Game.batch.draw(region, x * ts, y * ts);
                }
            }
        }

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

    public boolean editTile(int tX, int tY, int tT) {
        Vector2 tk = new Vector2(tX, tY);

        if (mapTiles.containsKey(tk)) {
            if (mapTiles.get(tk).type == tT) {
                return false;
            }
        }

        if (tT == -1) {
            mapTiles.remove(tk);
        } else {
            mapTiles.put(tk, new MapTile(tT));
        }
        Game.networking.send(new GameNetworkEditTileEvent(tX, tY, tT));

        return true;
    }

    public boolean checkCollision(Vector2 pos) {
        MapTile mapTile = tileBelow(pos);

        return mapTile != null && collideTiles.contains(mapTile.type);
    }

    public MapTile tileBelow(Vector2 pos) {
        pos = new Vector2((float) Math.floor(pos.x / Game.ts), (float) Math.floor(pos.y / Game.ts));

        if (mapTiles.containsKey(pos)) {
            return mapTiles.get(pos);
        }

        return null;
    }

    public static Vector2 snap(Vector2 pos, Vector2 lastPos) {
        return new Vector2(
                snap(pos.x, lastPos.x),
                snap(pos.y, lastPos.y)
        );
    }

    public static float snap(float pos, float lastPos) {
        pos = Math.round(pos / Game.ts) * Game.ts;

        if (pos < lastPos) {
            pos -= 0.0001f;
        }

        return pos;
    }

    public MapObject findObjectAt(Vector2 pos) {
        for (MapObject mapObject : mapObjects.values()) {
            if (new Rectangle(mapObject.pos.x, mapObject.pos.y, Game.ts, Game.ts).contains(pos)) {
                return mapObject;
            }
        }

        return null;
    }
}
