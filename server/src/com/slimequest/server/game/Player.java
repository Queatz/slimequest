package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.slimequest.server.Game;
import com.slimequest.server.events.GameStateEvent;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

/**
 * Created by jacob on 9/11/16.
 */

public class Player extends MapObject {
    public boolean frozen;

    @Override
    public JsonObject fossilize() {
        return super.fossilize();
    }

    @Override
    public void defossilize(JsonObject fossil) {
        super.defossilize(fossil);
    }

    @Override
    public String getType() {
        return GameType.PLAYER;
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.IDENTIFY.equals(event.getType())) {
            if (channel != null) {
                System.out.println("Sending event: " + event.json());
                channel.writeAndFlush(event.json());

                // Send game state
                String itPlayer = Game.world.getGameState().itPlayer;
                getEvent(new GameStateEvent(itPlayer));
            }
        }

        else if (GameEvent.GAME_STATE.equals(event.getType())) {
            System.out.println("Sending event: " + event.json());
            channel.writeAndFlush(event.json());
        }

        else if (GameEvent.MAP_TILES.equals(event.getType())) {
            if (channel != null) {
                System.out.println("Sending event: " + event.json());
                channel.writeAndFlush(event.json());
            }
        } else {
            super.getEvent(event);
        }
    }
}
