package com.slimequest.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.slimequest.game.game.MapObject;
import com.slimequest.game.game.World;

/**
 * Created by jacob on 9/11/16.
 */

public class Game {

    // The game
    public static SlimeQuestGame game;

    // The world
    public static World world = new World();

    // Networking stuffs...
    public static GameNetworking networking;

    // The current player
    public static MapObject player;
    public static String playerId;

    // A batch for drawing the world
    public static SpriteBatch batch = new SpriteBatch();

    // For drawing shapes
    public static ShapeRenderer shapeRenderer = new ShapeRenderer();

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
}
