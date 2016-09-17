package com.slimequest.server;

import com.slimequest.server.game.Map;
import com.slimequest.server.game.World;

import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;

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

    // The fossils
    public static ConcurrentMap<String, String> fossils;
    public static Thread mainThread;
    public static Channel channel;
}
