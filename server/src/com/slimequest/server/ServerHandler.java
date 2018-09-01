package com.slimequest.server;

import com.slimequest.server.events.GameNotificationEvent;
import com.slimequest.server.game.Player;
import com.slimequest.server.game.World;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.Json;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by jacob on 9/10/16.
 */


public class ServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, String msg) {
        System.out.println("Got event: " + msg);
        final GameNetworkEvent event = Json.from(msg, GameNetworkEvent.class);

        if (event == null) {
            System.out.println("Got fail event :( --> \"" + msg + "\"");
            return;
        }

        Game.world.post(new RunInWorld() {
            @Override
            public void runInWorld(World world) {
                world.getEvent(event);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Got new client: " + ctx.channel().id());

        // Identify

        final Player player = new Player();
        player.id = ctx.channel().id().asShortText();
        player.map = Game.startMap;
        player.x = 0;
        player.y = 0;
        player.channel = ctx.channel();

        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                player.channel = null; // Whether success or failure
            }
        });

        // When the client connects, add their object
        // XXX todo: object should've been persisted, and set as here / awake
        Game.world.add(player);

        Game.world.getEvent(new GameNotificationEvent(player.id, "joined!"));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Lost client: " + ctx.channel().id());

        final String id = ctx.channel().id().asShortText();

        // When the client disconnects, remove their object
        // XXX todo: persist, but set as away / sleeping
        Game.world.post(new RunInWorld() {
            @Override
            public void runInWorld(World world) {
                Game.world.remove(id);
                Game.world.getEvent(new GameNotificationEvent(id, "left the game"));
            }
        });
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

    }
}