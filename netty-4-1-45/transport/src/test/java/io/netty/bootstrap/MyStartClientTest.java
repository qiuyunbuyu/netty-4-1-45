package io.netty.bootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class MyStartClientTest {
	public static void main(String[] args) throws InterruptedException {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(new NioEventLoopGroup());
		bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
			@Override
			protected void initChannel(NioSocketChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler());
				ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
					@Override
					public void channelActive(ChannelHandlerContext ctx) throws Exception {
						ByteBufAllocator alloc = ctx.alloc();
						ByteBuf buffer = alloc.buffer();
						buffer.writeCharSequence("test from fishyu", Charset.defaultCharset());
						ctx.writeAndFlush(buffer);
					}
				});
			}
		});

		Channel channel = bootstrap.connect(new InetSocketAddress(8000)).sync().channel();
		channel.closeFuture().sync();
	}
}
