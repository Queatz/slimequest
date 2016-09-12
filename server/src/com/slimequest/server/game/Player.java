package com.slimequest.server.game;

import com.slimequest.shared.GameEvent;
import com.slimequest.shared.GameNetworkEvent;

/**
 * Created by jacob on 9/11/16.
 */

public class Player extends MapObject {
    @Override
    public void getEvent(GameNetworkEvent event) {
        if (GameEvent.IDENTIFY.equals(event.getType())) {
            if (channel != null) {
                channel.writeAndFlush(event.json());
            }
        } else {
            super.getEvent(event);
        }
    }
}
