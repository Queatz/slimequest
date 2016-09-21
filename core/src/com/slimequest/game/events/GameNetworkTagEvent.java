package com.slimequest.game.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.game.Game;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/20/16.
 */

public class GameNetworkTagEvent extends GameNetworkEvent {
    public GameNetworkTagEvent(String playerId) {
        super(GameEvent.TAG_PLAYER, new JsonObject());
        getData().getAsJsonObject().add(GameAttr.ID, new JsonPrimitive(Game.playerId));
        getData().getAsJsonObject().add(GameAttr.TAG, new JsonPrimitive(playerId));
    }
}
