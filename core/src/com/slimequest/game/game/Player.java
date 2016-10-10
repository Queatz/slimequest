package com.slimequest.game.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.slimequest.game.Debouncer;
import com.slimequest.game.Game;
import com.slimequest.game.GameNotification;
import com.slimequest.game.GameResources;
import com.slimequest.game.Misc;
import com.slimequest.game.events.GameNetworkMoveEvent;
import com.slimequest.game.events.GameNetworkTagEvent;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameNetworkEvent;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by jacob on 9/11/16.
 */

public class Player extends MapObject {

    private static long start = new Date().getTime();

    // Send pos to server in intervals
    private Debouncer movementDebouncer;
    public boolean frozen;
    public boolean hasEatenCarrot;
    private Date hasEatenCarrotExpiry;

    private Date lastTag = new Date();

    private enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private static java.util.Map<Direction, String[]> imageFromDirection = new HashMap<>();

    static {
        imageFromDirection.put(Direction.LEFT, new String[] { "Bunny-left-final.png", "Bunny-left-jump-final.png" });
        imageFromDirection.put(Direction.RIGHT, new String[] { "Bunny-right-final.png", "Bunny-right-jump-final.png" });
        imageFromDirection.put(Direction.UP, new String[] { "Bunny-back-final.png", "Bunny-back-jump-final.png" });
        imageFromDirection.put(Direction.DOWN, new String[] { "Bunny-front-final.png", "Bunny-front-jump-final.png" });
    }

    private Direction direction = Direction.DOWN;
    private int frame = 0;

    public Player() {
        super();
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        // Update frozen state of object
        if (event.getData().getAsJsonObject().has(GameAttr.FROZEN)) {
            frozen = event.getData().getAsJsonObject().get(GameAttr.FROZEN).getAsBoolean();

            if (Game.world.itPlayerId != null) {
                Game.gameNotifications.add(new GameNotification(this.id, frozen ? "frozen!" : "unfrozen!"));

                GameResources.snd(frozen ? "gotted.ogg" : "haha.ogg").play();
            }
        }
    }

    @Override
    public void update() {
        if (hasEatenCarrot) {
            if (new Date().after(hasEatenCarrotExpiry)) {
                hasEatenCarrotExpiry = null;
                hasEatenCarrot = false;
            }
        }

        if (Game.player.id.equals(id) && !pos.equals(lastPos)) {
            if (movementDebouncer == null) {
                movementDebouncer = new Debouncer(new Runnable() {
                    @Override
                    public void run() {
                        Game.networking.send(new GameNetworkMoveEvent(Game.player));
                    }
                }, 300);
            }

            movementDebouncer.debounce();
        }

        if (movementDebouncer != null) {
            movementDebouncer.update();
        }

        Vector2 last = new Vector2(lastPos);

        super.update();

        if (last.dst(pos) > .125f) {
            float xDiff = pos.x - last.x;
            float yDiff = pos.y - last.y;

            if (Math.abs(xDiff) > Math.abs(yDiff)) {
                if (xDiff > 0) {
                    direction = Direction.RIGHT;
                } else {
                    direction = Direction.LEFT;
                }
            } else {
                if (yDiff > 0) {
                    direction = Direction.UP;
                } else {
                    direction = Direction.DOWN;
                }
            }

            frame = (int) ((new Date().getTime() / 100) % 2);
        } else {
            frame = 0;
        }

        // Tagging bunnies
        if (id.equals(Game.playerId)) {
            // Can only tag once every 2 seconds
            if (lastTag.before(new Date(new Date().getTime() - 2000))) {
                if (map != null) {
                    for (MapObject mapObject : map.getMapObjects()) {
                        if (mapObject == this) {
                            continue;
                        }

                        // Can only tag players or butterflies when there's no player
                        if (!Player.class.isAssignableFrom(mapObject.getClass())) {
                            if (Game.world.itPlayerId != null || !Slime.class.isAssignableFrom(mapObject.getClass())) {
                                continue;
                            }
                        }

                        if (mapObject.pos.dst(pos) <= Game.ts) {
                            lastTag = new Date();

                            Game.networking.send(new GameNetworkTagEvent(mapObject.id));

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
        color.a = frozen ? .5f : 1f;
        Game.batch.setColor(color);

        Texture texture = GameResources.img(imageFromDirection.get(direction)[frame]);

        // XXX TODO handle case of no img loaded yet, draw random circle......
        // When have resource server....
        Game.batch.draw(texture, pos.x - texture.getWidth() / 2, pos.y - texture.getHeight() / 2);

        // Reset color
        Game.batch.setColor(1f, 1f , 1f, 1f);
    }

    public void eatCarrot() {
        hasEatenCarrot = true;

        if (hasEatenCarrotExpiry == null) {
            hasEatenCarrotExpiry = new Date();
        }

        hasEatenCarrotExpiry = new Date(hasEatenCarrotExpiry.getTime() + 1000 * 30);
    }

    public void snapTo(Vector2 pos) {
        this.pos.set(pos);
        Game.networking.send(new GameNetworkMoveEvent(this));
    }

    public static Color getBunnyColor(String id) {
        float color[] = Misc.HSVtoRGB(Misc.stringToFloat(id), .5f, 1f);

        return new Color(color[0], color[1], color[2], 1);
    }

}
