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

        System.out.println("Game loop start");

        // XXX Maybe handle server restarts and load the last shutdown time
        Date lastUpdate = new Date();
        while (true) {
            // Delta stuff
            Date newUpdate = new Date();
            float delta = newUpdate.getTime() - lastUpdate.getTime();
            lastUpdate = newUpdate;

            if (Game.world != null) {
                Game.world.update(delta);
            }

            // Game loop runs at 1/32th of a second
            try {
                Thread.sleep(32);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("Game loop finished");
    }
}
