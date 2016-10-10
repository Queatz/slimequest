package com.slimequest.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jacob on 9/18/16.
 */

public class MapTiles {

    // Map of tile IDs that are colliding
    public final static List<Set<Integer>> collideTiles = new ArrayList<>();

    static {
        Set<Integer> grassy = new HashSet<>();
        collideTiles.add(grassy);

        // TODO defined by tileset specification...
        // Needs support for groups
        // Format = y 00 x
        grassy.add(3000);

        grassy.add(3002);

        grassy.add(4);
        grassy.add(1004);
        grassy.add(6004);
        grassy.add(7004);

        grassy.add(5);
        grassy.add(1005);
        grassy.add(2005);
        grassy.add(3005);
        grassy.add(5005);
        grassy.add(6005);
        grassy.add(7005);

        grassy.add(6);
        grassy.add(1006);
        grassy.add(3006);
        grassy.add(5006);
        grassy.add(6006);
        grassy.add(7006);

        grassy.add(7);
        grassy.add(1007);
        grassy.add(2007);
        grassy.add(3007);
        grassy.add(5007);
        grassy.add(6007);

        Set<Integer> underground = new HashSet<>();
        collideTiles.add(underground);

        underground.add(0);
        underground.add(1);

        underground.add(4000);
        underground.add(5000);

        underground.add(4001);
        underground.add(5001);

        underground.add(4002);
        underground.add(5002);
    }
}
