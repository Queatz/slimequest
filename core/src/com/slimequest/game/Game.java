package com.slimequest.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.slimequest.game.game.MapObject;
import com.slimequest.game.game.World;

/**
 * Created by jacob on 9/11/16.
 */

public class Game {
    public static SpriteBatch batch = new SpriteBatch();
    public static World world = new World();
    public static GameNetworking networking;
    public static MapObject player;
}
