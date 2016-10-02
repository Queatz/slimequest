package com.slimequest.shared;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 9/18/16.
 */

public class MapTiles {

    // Map of tile IDs that are colliding
    public final static Set<Integer> collideTiles = new HashSet<>();

    static {
        // TODO defined by tileset specification...
        // Needs support for groups
        // Format = y 00 x
        collideTiles.add(3000);

        collideTiles.add(3002);

        collideTiles.add(4);
        collideTiles.add(1004);
        collideTiles.add(6004);
        collideTiles.add(7004);

        collideTiles.add(5);
        collideTiles.add(1005);
        collideTiles.add(2005);
        collideTiles.add(3005);
        collideTiles.add(5005);
        collideTiles.add(6005);
        collideTiles.add(7005);

        collideTiles.add(6);
        collideTiles.add(1006);
        collideTiles.add(3006);
        collideTiles.add(5006);
        collideTiles.add(6006);
        collideTiles.add(7006);

        collideTiles.add(7);
        collideTiles.add(1007);
        collideTiles.add(2007);
        collideTiles.add(3007);
        collideTiles.add(5007);
        collideTiles.add(6007);
    }
}
