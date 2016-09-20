package com.slimequest.server;

import com.slimequest.server.game.Map;
import com.slimequest.server.game.World;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
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



    private static DB db;
    private static int dbUsers = 0;

    public static void openDb() {
        dbUsers++;

        if (db != null) {
            return;
        }

        db = DBMaker.fileDB(new File("slime.db"))
                .fileMmapEnableIfSupported()
                .make();

        Game.fossils = db.hashMap("slimeWorld", Serializer.STRING, Serializer.STRING).createOrOpen();
    }

    public static void closeDb() {
        dbUsers--;

        if (db == null || dbUsers > 0) {
            return;
        }

        db.close();
        db = null;
        Game.fossils = null;
    }

}
