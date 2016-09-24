package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.slimequest.server.Game;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameType;
import com.slimequest.shared.Json;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by jacob on 9/15/16.
 */

public class Fossilize implements Serializable {
    private static java.util.Map<String, Class<? extends Fossilizeable>> fossilizers = new HashMap<>();

    public static void register(String type, Class<? extends Fossilizeable> fossilizeable) {
        fossilizers.put(type, fossilizeable);
    }

    static {
//        register(GameType.PLAYER, Player.class);
        register(GameType.MAP, Map.class);
        register(GameType.TELEPORT, Teleport.class);
        register(GameType.SLIME, Slime.class);
        register(GameType.SIGN, Sign.class);
//        register(GameType.WORLD, World.class);
    }

    // Create fossil from game object
    public static JsonObject fossilize(Fossilizeable fossilizeable) {
        JsonObject fossil = fossilizeable.fossilize();

        if (!fossil.has(GameAttr.ID) && fossil.has(GameAttr.TYPE)) {
            // Will not be able to unfossilize
            return null;
        }

        return fossil;
    }

    // Create game object from fossil
    public static Fossilizeable defossilize(JsonObject fossil) {
        String id = fossil.get(GameAttr.ID).getAsString();
        Fossilizeable fossilizeable = Game.world.get(id, false);

        if (fossilizeable == null) {
            fossilizeable = newInstance(fossil);
        }

        if (fossilizeable == null) {
            return null;
        }

        // Sneaky hack to subvert recursive gets
        ((GameObject) fossilizeable).id = id;
        Game.world.add((GameObject) fossilizeable);

        fossilizeable.defossilize(fossil);

        return fossilizeable;
    }

    public static Fossilizeable newInstance(JsonObject jsonObject) {
        try {
            Class<? extends Fossilizeable> clazz = fossilizers.get(jsonObject.get(GameAttr.TYPE).getAsString());

            if (clazz == null) {
                return null;
            }

            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
