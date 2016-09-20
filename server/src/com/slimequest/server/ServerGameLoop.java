package com.slimequest.server;

import com.slimequest.server.game.Map;
import com.slimequest.server.game.Slime;
import com.slimequest.server.game.World;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jacob on 9/14/16.
 */

public class ServerGameLoop extends Thread {

    private DB db;

    private void openDb() {
        db = DBMaker.fileDB(new File("slime.db"))
                .fileMmapEnableIfSupported()
                .make();

        Game.fossils = db.hashMap("slimeWorld", Serializer.STRING, Serializer.STRING).createOrOpen();
    }

    private void closeDb() {
        db.close();
        db = null;
        Game.fossils = null;
    }

    @Override
    public void run() {
        openDb();

        Game.world = new World();

        if (Game.fossils.isEmpty()) {
            Game.startMap = new Map();
            Game.startMap.id = "bunny";
            Game.world.add(Game.startMap);

            // Add other maps...
            Map map = new Map();
            map.id = "bunny2";
            Game.world.add(map);
            map = new Map();
            map.id = "underground";
            Game.world.add(map);

            // Add a slime :D
            Slime slime = new Slime();
            slime.map = Game.startMap;
            slime.id = "iamslime";
            Game.world.add(slime);
        }

        System.out.println("Loading game...");

        Game.world.load(Game.fossils);

        closeDb();

        final boolean[] alive = {true};

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                alive[0] = false;

                try {
                    Game.mainThread.join();
                } catch (InterruptedException ignore) {}
            }
        });

        if (Game.startMap == null) {
            Game.startMap = (Map) Game.world.get("bunny");
        }

        System.out.println("Game loop start");

        // XXX Maybe handle server restarts and load the last shutdown time
        Date lastUpdate = new Date();
        while (alive[0]) {
            // Delta stuff
            Date newUpdate = new Date();
            float delta = newUpdate.getTime() - lastUpdate.getTime();
            lastUpdate = newUpdate;

            if (Game.world != null) {
                try {
                    Game.world.update(delta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Game loop runs at 1/32th of a second
            try {
                Thread.sleep(32);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("Saving game...");

        openDb();
        Game.world.save(Game.fossils);
        closeDb();

        Game.channel.close();

        System.out.println("Game loop finished");
    }
}
