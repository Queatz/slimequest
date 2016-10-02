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
        collideTiles.add(3000);
        collideTiles.add(4000);
        collideTiles.add(5000);
        collideTiles.add(6000);
        collideTiles.add(7000);

        collideTiles.add(4001);
        collideTiles.add(5001);
        collideTiles.add(6001);
        collideTiles.add(7001);

        collideTiles.add(3002);
        collideTiles.add(4002);
        collideTiles.add(5002);
        collideTiles.add(6002);
        collideTiles.add(7002);

        collideTiles.add(4003);
        collideTiles.add(5003);

        collideTiles.add(4004);
        collideTiles.add(5004);
    }
}
