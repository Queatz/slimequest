package com.slimequest.game.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.game.game.GameObject;
import com.slimequest.game.game.MapObject;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/14/16.
 */

public class GameNetworkCreateObjectEvent extends GameNetworkEvent {
    public GameNetworkCreateObjectEvent(GameObject gameObject) {
        super(GameEvent.CREATE_OBJECT, new JsonObject());
        getData().getAsJsonObject().add(GameAttr.ID, new JsonPrimitive(gameObject.id));

        // XXX todo maybe not always right... hackish for now
        getData().getAsJsonObject().add(GameAttr.TYPE, new JsonPrimitive(gameObject.getClass().getSimpleName().toLowerCase()));

        if (MapObject.class.isAssignableFrom(gameObject.getClass())) {
            getData().getAsJsonObject().add(GameAttr.MAP, new JsonPrimitive(((MapObject) gameObject).map.id));
            getData().getAsJsonObject().add(GameAttr.X, new JsonPrimitive(((MapObject) gameObject).pos.x));
            getData().getAsJsonObject().add(GameAttr.Y, new JsonPrimitive(((MapObject) gameObject).pos.y));
        }
    }
}
