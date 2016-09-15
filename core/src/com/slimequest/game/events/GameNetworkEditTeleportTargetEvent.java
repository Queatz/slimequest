package com.slimequest.game.events;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.game.game.Teleport;
import com.slimequest.shared.GameAttr;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/14/16.
 */
public class GameNetworkEditTeleportTargetEvent extends GameNetworkEvent {
    public GameNetworkEditTeleportTargetEvent(Teleport teleport, String target) {
        super(GameEvent.EDIT_TELEPORT_TARGET, new JsonObject());

        getData().getAsJsonObject().add(GameAttr.ID, new JsonPrimitive(teleport.id));
        getData().getAsJsonObject().add(GameAttr.DATA, new JsonPrimitive(target));
    }
}
