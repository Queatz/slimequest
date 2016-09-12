package com.slimequest.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.slimequest.server.game.MapObject;
import com.slimequest.server.game.Player;
import com.slimequest.shared.GameNetworkEvent;
import com.slimequest.shared.Json;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.DefaultEventExecutor;

/**
 * Created by jacob on 9/10/16.
 */


public class ServerHandler extends SimpleChannelInboundHandler<String> {

    // LOL needs to be static because thus class is per client
    private final static ChannelGroup channels = new DefaultChannelGroup(new DefaultEventExecutor());

    public static JsonObject playerJson(MapObject player) {
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive(player.id));
        json.add("x", new JsonPrimitive(player.x));
        json.add("y", new JsonPrimitive(player.y));
        return json;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("Got event: " + msg);
        GameNetworkEvent event = Json.from(msg, GameNetworkEvent.class);

        if (event == null) {
            System.out.println("Got fail event :( --> \"" + msg + "\"");
            return;
        }

        Game.world.getEvent(event);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Got new client: " + ctx.channel().id());

        // Identify

        final Player player = new Player();
        player.id = ctx.channel().id().asShortText();
        player.x = 0;
        player.y = 0;
        player.channel = ctx.channel();

        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                player.channel = null; // Whether success or failure
            }
        });

        Game.world.add(player);
        channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Lost client: " + ctx.channel().id());

        channels.remove(ctx.channel());
        Game.world.remove(ctx.channel().id().asShortText());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {

    }
}