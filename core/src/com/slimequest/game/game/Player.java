package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.google.gson.JsonPrimitive;
import com.slimequest.game.Debouncer;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;
import com.slimequest.game.events.GameNetworkMoveEvent;
import com.slimequest.shared.GameAttr;

import java.util.Random;

/**
 * Created by jacob on 9/11/16.
 */

public class Player extends MapObject {
    private Debouncer movementDebouncer;
    private Vector2 lastPos = new Vector2();

    @Override
    public void update() {
        super.update();

        if (Game.player.id.equals(id) && !pos.equals(lastPos)) {
            if (movementDebouncer == null) {
                movementDebouncer = new Debouncer(new Runnable() {
                    @Override
                    public void run() {
                        Game.networking.send(new GameNetworkMoveEvent(Game.player));
                    }
                });
            }

            lastPos.set(pos);

            movementDebouncer.debounce();
        }

        if (movementDebouncer != null) {
            movementDebouncer.update();
        }
    }

    @Override
    public void render() {
        Random rnd = new Random(id.hashCode());
        Game.batch.setColor(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat(), 1f);

        Texture texture = GameResources.img("badlogic.png");

        // XXX TODO handle case of no img loaded yet, draw random circle......
        Game.batch.draw(texture, pos.x - texture.getWidth() / 2, pos.y - texture.getHeight() / 2);

        // Reset color
        Game.batch.setColor(1f, 1f , 1f, 1f);
    }

    public void snapTo(Vector2 pos) {
        this.pos.set(pos);
        Game.networking.send(new GameNetworkMoveEvent(this));
    }
}
