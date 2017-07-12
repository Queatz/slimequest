package com.slimequest.server.game;

import com.slimequest.server.Game;
import com.slimequest.server.GameArangoDb;
import com.slimequest.server.events.GameStateEvent;
import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.GameType;

import java.util.Date;

/**
 * Created by jacob on 10/7/16.
 */

public class Carrot extends MapObject {
    private Date popsAt;
    public boolean isEaten;

    @Override
    public String getType() {
        return GameType.CARROT;
    }

    @Override
    public void update(float delta) {
        // If past popsAt, popsAt = null && send update to clients that I am ready

        if (popsAt != null && new Date().after(popsAt)) {
            isEaten = false;
            popsAt = null;
            Game.world.getEvent(new GameNetworkEvent(GameEvent.OBJECT_STATE, Game.objJson(this)));
            GameArangoDb.save(this);
        }
    }

    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.TAG_PLAYER.equals(event.getType())) {
            isEaten = true;

            // Reload in 2 minutes
            popsAt = new Date(new Date().getTime() + 1000 * 60 * 2);
            Game.world.getEvent(new GameNetworkEvent(GameEvent.OBJECT_STATE, Game.objJson(this)));
            GameArangoDb.save(this);
        }
    }
}
