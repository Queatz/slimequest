package com.slimequest.server;

import com.slimequest.server.game.Map;
import com.slimequest.server.game.World;

/**
 * Created by jacob on 9/11/16.
 */

public class Game {

    // The world
    public static World world;

    // The starting map. Players start at 0, 0
    public static Map startMap;

    // Game tile size
    public static int ts = 16;
}
