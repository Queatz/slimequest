package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.slimequest.server.Game;
import com.slimequest.server.RunInWorld;
import com.slimequest.server.events.GameStateEvent;
import com.slimequest.shared.EventAttr;
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

                // Send initial game state also
                String itPlayer = Game.world.getGameState().itPlayer;
                getEvent(new GameStateEvent(itPlayer));
            }
        }

        // Notify client of game state changes
        else if (GameEvent.GAME_STATE.equals(event.getType())) {
            System.out.println("Sending event: " + event.json());
            channel.writeAndFlush(event.json());
        }

        // Notify client of map tile changes
        else if (GameEvent.MAP_TILES.equals(event.getType())) {
            if (channel != null) {
                System.out.println("Sending event: " + event.json());
                channel.writeAndFlush(event.json());
            }
        } else if (GameEvent.TAG_PLAYER.equals(event.getType())) {
            String playerId = EventAttr.getId(event);

            // Shouldn't happen, but check anyways
            if (!id.equals(playerId)) {
                return;
            }

            final String otherId = EventAttr.getTag(event);
            GameObject other = Game.world.get(otherId);
            GameState gameState = Game.world.getGameState();

            // Tagging the itPlayer means nothing
            // If there is no itPlayer, then no tagging should occur
            if (gameState.itPlayer == null || otherId.equals(gameState.itPlayer)) {
                return;
            }

            // Check if the other object is a player and freeze them!
            if (other != null && Player.class.isAssignableFrom(other.getClass())) {
                ((Player) other).frozen = playerId.equals(gameState.itPlayer);
                ((Player) other).map.getEvent(new GameNetworkEvent(GameEvent.OBJECT_STATE, Game.objJson((Player) other)));
            }

            // Check if the game has ended

            boolean gameOver = true;

            for (GameObject gameObject : Game.world.getObjects().values()) {
                if (Player.class.isAssignableFrom(gameObject.getClass()) && !((Player) gameObject).frozen && !gameObject.id.equals(gameState.itPlayer)) {
                    gameOver = false;
                    break;
                }
            }

            // If all players except the itPlayer are frozen then the game ends
            if (gameOver) {
                gameState.itPlayer = null;
                getEvent(new GameStateEvent(null));

                // Next game, everyone unfreeze!
                for (GameObject gameObject : Game.world.getObjects().values()) {
                    if (Player.class.isAssignableFrom(gameObject.getClass()) && ((Player) gameObject).frozen) {
                        ((Player) gameObject).frozen = false;
                        ((Player) gameObject).map.getEvent(new GameNetworkEvent(GameEvent.OBJECT_STATE, Game.objJson((Player) gameObject)));
                        break;
                    }
                }

                // Next game starts in 5 seconds...
                Game.world.post(new RunInWorld() {
                    @Override
                    public void runInWorld(World world) {
                        // Last player to be tagged becomes it
                        Game.world.getGameState().itPlayer = otherId;
                        getEvent(new GameStateEvent(otherId));
                    }
                }, 5000);
            }
        } else {
            super.getEvent(event);
        }
    }
}
