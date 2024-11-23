package io.netty.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.LoggingHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class MyStartTest {
	public static void main(String[] args) throws InterruptedException {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.group(new NioEventLoopGroup());
		serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
			@Override
			protected void initChannel(NioSocketChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler());
			}
		});

		// 大部分初始化在bind中完成
		Channel channel = serverBootstrap.bind(8000).sync().channel();
		channel.closeFuture().sync();
	}
}
