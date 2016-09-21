package com.slimequest.server.game;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.Game;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

import io.netty.channel.Channel;

/**
 * Created by jacob on 9/11/16.
 */

public abstract class GameObject implements Fossilizeable {
    public String id;
    public Channel channel;

    @Override
    public JsonObject fossilize() {
        JsonObject fossil = new JsonObject();
        fossil.add(GameAttr.ID, new JsonPrimitive(id));
        fossil.add(GameAttr.TYPE, new JsonPrimitive(getType()));
        return fossil;
    }

    @Override
    public void defossilize(JsonObject fossil) {
        id = fossil.get(GameAttr.ID).getAsString();
    }

    public String getType() {
        return GameType.OBJECT;
    }

    public void getEvent(GameNetworkEvent event) {

    }

    public void update(float delta) {

    }
}
