package io.netty.bootstrap;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.TimeUnit;

public class EventLoopThread {
	public static void main(String[] args) {
		NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		eventLoopGroup.setIoRatio(80);

		EventLoop eventLoop = eventLoopGroup.next();
		eventLoop.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("test common task");
			}
		});

		eventLoop.schedule(new Runnable() {
			@Override
			public void run() {
				System.out.println("test schedule");
			}
		}, 5000, TimeUnit.MILLISECONDS);
	}
}
