package com.slimequest.game.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.game.Game;
import com.slimequest.game.game.MapObject;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/12/16.
 */
public class GameNetworkMoveEvent extends GameNetworkEvent {
    public GameNetworkMoveEvent(MapObject mapObject) {
        super(GameEvent.MOVE, new JsonObject());
        getData().getAsJsonObject().add(GameAttr.ID, new JsonPrimitive(mapObject.id));
        getData().getAsJsonObject().add(GameAttr.X, new JsonPrimitive(mapObject.pos.x));
        getData().getAsJsonObject().add(GameAttr.Y, new JsonPrimitive(mapObject.pos.y));
    }
}
