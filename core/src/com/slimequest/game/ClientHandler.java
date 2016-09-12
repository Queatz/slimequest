package com.slimequest.game;

import com.slimequest.game.game.Player;
import com.slimequest.shared.GameEvent;
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
        GameNetworkEvent event = Json.from(msg, GameNetworkEvent.class);

        if (event == null) {
            Logger.getAnonymousLogger().warning("Could not parse event: " + msg);
            return;
        }

        if (GameEvent.IDENTIFY.equals(event.getType())) {
            Game.player = new Player();
            Game.player.id = event.getData().getAsJsonObject().get("id").getAsString();
            Game.player.x = event.getData().getAsJsonObject().get("x").getAsInt();
            Game.player.y = event.getData().getAsJsonObject().get("y").getAsInt();
            Game.world.add(Game.player);
        } else {
            Game.world.getEvent(event);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
