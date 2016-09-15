package com.slimequest.game.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.game.game.MapObject;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/14/16.
 */
public class GameNetworkRemoveObjectEvent extends GameNetworkEvent {
    public GameNetworkRemoveObjectEvent(MapObject mapObject) {
        super(GameEvent.REMOVE_OBJECT, new JsonObject());
        getData().getAsJsonObject().add("id", new JsonPrimitive(mapObject.id));
    }
}
