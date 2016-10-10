package com.slimequest.game.game;

import com.badlogic.gdx.math.Vector2;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;

/**
 * Created by jacob on 9/14/16.
 */

public class Teleport extends MapObject {

    private boolean dontTrigger = true;

    @Override
    public void update() {
        if (Game.player == null) {
            return;
        }

        // Client side teleporation of player
        if (dontTrigger) {
            if (!doesTrigger(Game.player)) {
                dontTrigger = false;
            }
        } else {
            if (doesTrigger(Game.player)) {
                ((Player) Game.player).snapTo(new Vector2(this.pos.x + Game.ts / 2, this.pos.y + Game.ts / 2));
                dontTrigger = true;
            }
        }
    }

    @Override
    public void render() {
        if (Game.isEditing) {
            Game.batch.draw(GameResources.img("teleport_edit_mode.png"), pos.x, pos.y);
        }
    }
}
