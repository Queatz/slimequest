package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;

import java.util.Random;

/**
 * Created by jacob on 9/11/16.
 */

public class Slime extends MapObject {
    @Override
    public void render() {
        Texture texture = GameResources.img("butterfly.png");
        Game.batch.draw(texture, pos.x - texture.getWidth() / 2, pos.y);
    }
}
