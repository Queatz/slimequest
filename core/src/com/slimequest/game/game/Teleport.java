package com.slimequest.game.game;

import com.slimequest.game.Game;
import com.slimequest.game.GameResources;

/**
 * Created by jacob on 9/14/16.
 */

public class Teleport extends MapObject {
    @Override
    public void update() {
        // ... If tapped in edit mode, ask to remove
        // ... Find nearby bunnies and butterflies and transport them :)
    }

    @Override
    public void render() {
        if (Game.isEditing) {
            Game.batch.draw(GameResources.img("teleport_edit_mode.png"), pos.x, pos.y);
        }
    }
}
