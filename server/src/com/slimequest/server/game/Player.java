package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.slimequest.server.Game;
import com.slimequest.server.RunInWorld;
import com.slimequest.server.events.GameNotificationEvent;
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
                getEvent(new GameStateEvent(Game.world.whosIt()));

                // First player to be it is the one that finds the butterfly
                if (Game.world.whosIt() == null) {
                    getEvent(new GameNotificationEvent(":butterfly", "find the\nbutterfly!"));
                }
            }
        }

        // Notify client of game state changes
        else if (GameEvent.GAME_STATE.equals(event.getType())) {
            if (channel != null) {
                System.out.println("Sending event: " + event.json());
                channel.writeAndFlush(event.json());
            }
        }

        // Notify client of map tile changes
        else if (GameEvent.MAP_TILES.equals(event.getType())) {
            if (channel != null) {
                System.out.println("Sending event: " + event.json());
                channel.writeAndFlush(event.json());
            }
        }

        // Send game notifications
        else if (GameEvent.GAME_NOTIFICATION.equals(event.getType())) {
            if (id.equals(EventAttr.getId(event)) && !EventAttr.getSelfReceive(event)) {
                return;
            }

            if (channel != null) {
                System.out.println("Sending event: " + event.json());
                channel.writeAndFlush(event.json());
            }
        }

        else if (GameEvent.TAG_PLAYER.equals(event.getType())) {
            String playerId = EventAttr.getId(event);

            // Shouldn't happen, but check anyways
            if (!id.equals(playerId)) {
                return;
            }

            // Frozen players cannot unfreeze others
            if (frozen) {
                return;
            }

            final String otherId = EventAttr.getTag(event);
            GameObject other = Game.world.get(otherId);
            String itPlayer = Game.world.whosIt();

            // Tagging the itPlayer means nothing
            if (otherId.equals(itPlayer)) {
                return;
            }

            // If there is no itPlayer, then no tagging should occur besides the butterflies
            if (itPlayer == null) {
                if (!Slime.class.isAssignableFrom(other.getClass())) {
                    return;
                }

                Game.world.setWhosIt(playerId);

                return;
            }


            // Interaction only happens between players
            if (other == null || !Player.class.isAssignableFrom(other.getClass())) {
                return;
            }

            boolean freeze = playerId.equals(Game.world.whosIt());

            // Nothing to do
            if (((Player) other).frozen == freeze) {
                return;
            }

            // Freeze them! Or unfreeze if not the player
            ((Player) other).frozen = freeze;
            ((Player) other).map.getEvent(new GameNetworkEvent(GameEvent.OBJECT_STATE, Game.objJson((Player) other)));

            // Check if the game has ended

            boolean gameOver = true;

            for (GameObject gameObject : Game.world.getObjects().values()) {
                if (Player.class.isAssignableFrom(gameObject.getClass()) && !((Player) gameObject).frozen && !gameObject.id.equals(Game.world.whosIt())) {
                    gameOver = false;
                    break;
                }
            }

            // If all players except the itPlayer are frozen then the game ends
            if (gameOver) {
                String winner = Game.world.whosIt();
                Game.world.setWhosIt(null);
                Game.world.getEvent(new GameNotificationEvent(winner, "wins!", true));

                // Next game, everyone unfreeze!
                for (GameObject gameObject : Game.world.getObjects().values()) {
                    if (Player.class.isAssignableFrom(gameObject.getClass()) && ((Player) gameObject).frozen) {
                        ((Player) gameObject).frozen = false;
                        ((Player) gameObject).map.getEvent(new GameNetworkEvent(GameEvent.OBJECT_STATE, Game.objJson((Player) gameObject)));
                    }
                }

                // Next game starts in 10 seconds...
                Game.world.post(new RunInWorld() {
                    @Override
                    public void runInWorld(World world) {
                        // Last player to be tagged becomes it
                        Game.world.setWhosIt(otherId);
                        Game.world.getEvent(new GameStateEvent(otherId));

                    }
                }, 7000);
            }
        } else {
            super.getEvent(event);
        }
    }
}
