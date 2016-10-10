package com.slimequest.game.game;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by jacob on 9/11/16.
 */

public class MapTile {
    int type;

    // TODO Group not supported yet
    int group;

    public MapTile(int type, int group) {
        this.type = type;
        this.group = group;
    }

    /*
         * Get the X coordinate of a tile in the texture
         */
    public static int getX(MapTile tile) {
        return tile.type % 1000;
    }

    /*
     * Get the Y coordinate of a tile in the texture
     */
    public static int getY(MapTile tile) {
        return tile.type / 1000;
    }

    /*
     * Get the tileId from coordinates of the the texture
     */
    public static int getId(Vector2 pos) {
        return ((int) Math.floor(pos.x / 16f) % 1000) +
                ((int) Math.floor(pos.y / 16f) * 1000);
    }
}
