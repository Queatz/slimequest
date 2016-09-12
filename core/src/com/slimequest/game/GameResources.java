package com.slimequest.game;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 9/11/16.
 */

public class GameResources {
    public static Map<String, Texture> images = new HashMap<String, Texture>();

    public static Texture img(String img) {
        if (images.containsKey(img)) {
            return images.get(img);
        }

        Texture texture = new Texture(img);
        images.put(img, texture);
        return texture;
    }

    public static void dispose() {
        for (Texture texture : images.values()) {
            texture.dispose();
        }
    }
}
