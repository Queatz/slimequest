package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;

import java.util.Date;
import java.util.Random;

/**
 * Created by jacob on 9/11/16.
 */

public class Slime extends MapObject {
    @Override
    public void render() {
        int frame = (int) ((new Date().getTime() / 200) % 2);
        Texture texture = GameResources.img(frame == 0 ? "butterfly.png" : "butterfly-move.png");
        Game.batch.draw(texture, pos.x - texture.getWidth() / 2, pos.y);
    }
}
