package com.slimequest.server;

import com.slimequest.server.game.Map;
import com.slimequest.server.game.Slime;
import com.slimequest.server.game.World;

import java.util.Date;

/**
 * Created by jacob on 9/14/16.
 */

public class ServerGameLoop extends Thread {

    @Override
    public void run() {
        Game.world = new World();

        GameArangoDb.loadAll();

        if (GameArangoDb.isEmpty()) {
            Game.startMap = new Map();
            Game.startMap.id = "bunny";
            Game.world.add(Game.startMap);
            GameArangoDb.save(Game.startMap);

            // Add other maps...
            Map map = new Map();
            map.id = "bunny2";
            Game.world.add(map);
            map = new Map();
            map.id = "underground";
            Game.world.add(map);
            GameArangoDb.save(map);

            // Add a slime :D
            Slime slime = new Slime();
            slime.map = Game.startMap;
            slime.id = "iamslime";
            Game.world.add(slime);
            GameArangoDb.save(slime);
        }

        System.out.println("Loading game...");

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

        GameArangoDb.saveAll();
        Game.channel.close();

        System.out.println("Game loop finished");
    }
}
