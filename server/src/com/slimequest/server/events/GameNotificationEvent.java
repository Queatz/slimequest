package com.slimequest.server.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/22/16.
 */

public class GameNotificationEvent extends GameNetworkEvent {
    public GameNotificationEvent(String id, String message) {
        this(id, message, false);
    }

    public GameNotificationEvent(String id, String message, boolean selfReceive) {
        super(GameEvent.GAME_NOTIFICATION, new JsonObject());
        getData().getAsJsonObject().add(GameAttr.ID, new JsonPrimitive(id));
        getData().getAsJsonObject().add(GameAttr.DATA, new JsonPrimitive(message));
        getData().getAsJsonObject().add(GameAttr.SELF_RECEIVE, new JsonPrimitive(selfReceive));
    }
}
