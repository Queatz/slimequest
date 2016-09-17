package com.slimequest.server.game;

import com.google.gson.JsonObject;

/**
 * Created by jacob on 9/15/16.
 */
public interface Fossilizeable {
    JsonObject fossilize();
    void defossilize(JsonObject fossil);
}
