package com.slimequest.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.game.Carrot;
import com.slimequest.server.game.Map;
import com.slimequest.server.game.MapObject;
import com.slimequest.server.game.Player;
import com.slimequest.server.game.World;
import com.slimequest.shared.GameAttr;

import io.netty.channel.Channel;

/**
 * Created by jacob on 9/11/16.
 */

public class Game {

    // The world
    public static World world;

    // The starting map. Players start at 0, 0
    public static Map startMap;

    // Game tile size
    public static int ts = 16;

    public static Thread mainThread;
    public static Channel channel;

    // XXX need proper way to construct events with related data
    public static JsonObject objJson(MapObject obj) {
        JsonObject json = new JsonObject();
        json.add(GameAttr.ID, new JsonPrimitive(obj.id));
        json.add(GameAttr.TYPE, new JsonPrimitive(obj.getType()));
        json.add(GameAttr.X, new JsonPrimitive(obj.x));
        json.add(GameAttr.Y, new JsonPrimitive(obj.y));
        json.add(GameAttr.MAP, new JsonPrimitive(obj.map.id));

        if (Player.class.isAssignableFrom(obj.getClass())) {
            json.add(GameAttr.FROZEN, new JsonPrimitive(((Player) obj).frozen));
        }

        else if (Carrot.class.isAssignableFrom(obj.getClass())) {
            json.add(GameAttr.EATEN, new JsonPrimitive(((Carrot) obj).isEaten));
        }

        return json;
    }
}
