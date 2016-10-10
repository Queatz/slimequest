package com.slimequest.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.slimequest.game.game.MapObject;
import com.slimequest.game.game.World;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by jacob on 9/11/16.
 */

public class Game {

    // The game
    public static SlimeQuestGame game;

    // The world
    public static World world;

    // Noti queue
    public static ConcurrentLinkedQueue<GameNotification> gameNotifications = new ConcurrentLinkedQueue<>();

    // Networking stuffs...
    public static GameNetworking networking;

    // The current player
    public static MapObject player;
    public static String playerId;

    // A batch for drawing the world
    public static SpriteBatch batch;

    // For drawing shapes
    public static ShapeRenderer shapeRenderer;

    // The game viewport size, as a square
    public static int viewportSize = 160;

    // The current viewport center
    public static Vector2 viewportXY;

    // The size of tiles in the game
    public static int ts = 16;

    // Whether or not the game is in edit mode
    public static boolean isEditing;

    // Game font
    public static BitmapFont font;

    // Connection error
    public static boolean connectionError;
    public static boolean connecting;
    public static String serverAddress = "104.198.99.77";
    public static boolean alive;

    public static String credits = "[ROYAL]Bunny Freeze Tag\n\n" +

            "[WHITE]~ Credits ~\n\n" +

            "[ROYAL]Artwork\n" +
            "[WHITE]Mai Pham\n\n" +

            "[ROYAL]Programming\n" +
            "[WHITE]Jacob Ferrero\n\n" +

            "[ROYAL]Music\n" +
            "[WHITE]Jacob Ferrero\n\n" +

            "[ROYAL]Sound Effects\n" +
            "[WHITE]Jacob Ferrero\n\n" +

            "[ROYAL]Level Design\n" +
            "[WHITE]Mai Pham\n" +
            "[WHITE]Jacob Ferrero\n\n" +

            "[ROYAL]Font\n" +
            "[WHITE]basis33 by Manchson\n\n" +


            "\n[FOREST]Released 1/11/2016\n\n" +

            ""
            ;

    // local
    static {
        serverAddress = "192.168.0.107";
    }
}
