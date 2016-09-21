package com.slimequest.server.events;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/20/16.
 */

public class GameStateEvent extends GameNetworkEvent {
    public GameStateEvent(String itPlayer) {
        super(GameEvent.GAME_STATE, new JsonObject());

        if (itPlayer == null) {
            getData().getAsJsonObject().add(GameAttr.IT_PLAYER, JsonNull.INSTANCE);
        } else {
            getData().getAsJsonObject().add(GameAttr.IT_PLAYER, new JsonPrimitive(itPlayer));
        }
    }
}
