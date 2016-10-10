package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;

/**
 * Created by jacob on 9/23/16.
 */

public class Sign extends MapObject {
    private boolean dontTrigger = true;

    @Override
    public void update() {
        if (Game.player == null) {
            return;
        }

        if (dontTrigger) {
            if (!doesTrigger(Game.player)) {
                dontTrigger = false;
            }
        } else {
            if (doesTrigger(Game.player)) {
                Game.game.showCredits();
                dontTrigger = true;
            }
        }
    }

    @Override
    public void render() {
        Texture texture = GameResources.img("sign.png");
        Game.batch.draw(texture, pos.x, pos.y);
    }
}
