package io.netty.bootstrap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MyStartServerTest {
	private static final Logger log = LoggerFactory.getLogger(MyStartServerTest.class);
	public static void main(String[] args) throws InterruptedException {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.group(new NioEventLoopGroup());
		serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
			@Override
			protected void initChannel(NioSocketChannel ch) throws Exception {
				ch.pipeline().addLast(new LoggingHandler());
				ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						if (msg instanceof ByteBuf) {
							ByteBuf buf = (ByteBuf) msg;
							String received = buf.toString(CharsetUtil.UTF_8);
							log.debug("{}", received);
							buf.release();
						}
					}
				});
			}
		});

		// 大部分初始化在bind中完成
		Channel channel = serverBootstrap.bind(8000).sync().channel();
		channel.closeFuture().sync();
	}
}
