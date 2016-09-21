package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.slimequest.game.Debouncer;
import com.slimequest.game.Game;
import com.slimequest.game.GameResources;
import com.slimequest.game.Misc;
import com.slimequest.game.events.GameNetworkMoveEvent;
import com.slimequest.game.events.GameNetworkTagEvent;

import java.awt.Color;
import java.util.Date;

/**
 * Created by jacob on 9/11/16.
 */

public class Player extends MapObject {

    // Send pos to server in intervals
    private Debouncer movementDebouncer;
    public boolean frozen;

    private Date lastTag = new Date();

    public Player() {
        super();

        movementDebouncer = new Debouncer(new Runnable() {
            @Override
            public void run() {
                if (Game.networking != null) {
                    Game.networking.send(new GameNetworkMoveEvent(Game.player));
                }
            }
        }, 300);
    }

    @Override
    public void update() {
        super.update();

        if (Game.player.id.equals(id) && !pos.equals(lastPos)) {
            movementDebouncer.debounce();
        }

        movementDebouncer.update();

        // Tagging bunnies
        if (id.equals(Game.playerId)) {
            // Can only tag once every 5 seconds
            if (lastTag.before(new Date(new Date().getTime() - 5000))) {
                if (map != null) {
                    for (MapObject mapObject : map.getMapObjects()) {
                        if (mapObject == this) {
                            continue;
                        }

                        if (mapObject.pos.dst(pos) <= Game.ts) {
                            lastTag = new Date();

                            if (Game.networking != null) {
                                Game.networking.send(new GameNetworkTagEvent(mapObject.id));
                            }

                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void render() {
        Color color = getBunnyColor(id);
        Game.batch.setColor(color.getRed(), color.getGreen(), color.getBlue(), frozen ? .5f : 1f);

        Texture texture = GameResources.img("badlogic.png");

        // XXX TODO handle case of no img loaded yet, draw random circle......
        // When have resource server....
        Game.batch.draw(texture, pos.x - texture.getWidth() / 2, pos.y - texture.getHeight() / 2);

        // Reset color
        Game.batch.setColor(1f, 1f , 1f, 1f);
    }

    public void snapTo(Vector2 pos) {
        this.pos.set(pos);
        Game.networking.send(new GameNetworkMoveEvent(this));
    }

    public static Color getBunnyColor(String id) {
        return java.awt.Color.getHSBColor(Misc.stringToFloat(id), 1f, 1f);
    }

}
