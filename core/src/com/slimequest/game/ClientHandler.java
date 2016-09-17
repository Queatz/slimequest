package com.slimequest.game;

import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.Json;

import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by jacob on 9/10/16.
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        final GameNetworkEvent event = Json.from(msg, GameNetworkEvent.class);

        if (event == null) {
            Logger.getAnonymousLogger().warning("Could not parse event: " + msg);
            return;
        }

        Game.game.post(new RunInGame() {
            @Override
            public void runInGame() {
                Game.world.getEvent(event);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        Game.connectionError = true;
    }
}
