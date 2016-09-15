package com.slimequest.server.game;

import com.slimequest.shared.GameNetworkEvent;

import io.netty.channel.Channel;

/**
 * Created by jacob on 9/11/16.
 */

public abstract class GameObject {
    public String id;
    public Channel channel;

    public abstract String getType();

    public void getEvent(GameNetworkEvent event) {

    }

    public void update(float delta) {

    }
}
