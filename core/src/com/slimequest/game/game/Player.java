package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;

import java.util.Random;

/**
 * Created by jacob on 9/11/16.
 */

public class Player extends MapObject {

    @Override
    public void render() {
        Random rnd = new Random(id.hashCode());
        Game.batch.setColor(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat(), 1f);

        Texture texture = GameResources.img("badlogic.png");
        Game.batch.draw(texture, x - texture.getWidth() / 2, y - texture.getHeight() / 2);
    }
}
