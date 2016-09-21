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
}
