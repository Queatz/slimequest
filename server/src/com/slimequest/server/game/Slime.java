package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.slimequest.shared.GameType;

/**
 * Created by jacob on 9/11/16.
 */

public class Slime extends MapObject {
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
        return GameType.SLIME;
    }
}
