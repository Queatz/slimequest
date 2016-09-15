package com.slimequest.game.events;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.game.Game;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/13/16.
 */

public class GameNetworkEditTileEvent extends GameNetworkEvent {
    public GameNetworkEditTileEvent(int x, int y, int t) {
        super(GameEvent.EDIT_TILE, new JsonObject());

        getData().getAsJsonObject().add("id", new JsonPrimitive(Game.player.id));

        JsonArray tile = new JsonArray();
        tile.add(x);
        tile.add(y);
        tile.add(t);
        getData().getAsJsonObject().add("tile", tile);
    }
}
