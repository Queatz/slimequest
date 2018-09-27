package com.slimequest.server.game;

import com.slimequest.server.Game;
import com.slimequest.shared.GameType;

import java.awt.Point;
import java.util.Random;

/**
 * Created by jacob on 9/11/16.
 */

public class Slime extends MapObject {
    @Override
    public String getType() {
        return GameType.SLIME;
    }

    @Override
    public void update(float dt) {
        // Slimes randomly move around
        if (map != null && new Random().nextInt((int) (3 * dt + 1)) == 0) {
            int newX = x + new Random().nextInt(Game.ts * 4) - Game.ts * 2;
            int newY = y + new Random().nextInt(Game.ts * 4) - Game.ts * 2;

            if (!map.checkCollision(new Point(newX, newY))) {
                Game.world.move(this, newX, newY);
            }
        }
    }
}
