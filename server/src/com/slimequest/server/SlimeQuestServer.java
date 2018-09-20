package com.slimequest.server;


import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.compression.SnappyFrameDecoder;
import io.netty.handler.codec.compression.SnappyFrameEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Just initiation stuff...
 */
public class SlimeQuestServer {

    private int port;

    private SlimeQuestServer(int port) {
        this.port = port;
    }

    private void run() throws Exception {

        // Start the game loop
        ServerGameLoop serverGameLoop = new ServerGameLoop();
        serverGameLoop.start();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                            // Track client
                        }

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Unpooled.wrappedBuffer(new byte[]{'\n'})))
                                    .addLast(new SnappyFrameDecoder())
                                    .addLast(new SnappyFrameEncoder())
                                    .addLast(new StringDecoder(Charset.forName("UTF-8")))
                                    .addLast(new StringEncoder(Charset.forName("UTF-8")))
                                    .addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections
            Game.channel = b.bind(port).sync().channel();
            Game.channel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        Game.mainThread = Thread.currentThread();

        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 3222;
        }
        new SlimeQuestServer(port).run();
    }
}