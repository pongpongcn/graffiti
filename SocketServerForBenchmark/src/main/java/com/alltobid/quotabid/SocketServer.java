package com.alltobid.quotabid;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

@Component
public class SocketServer {
	private static final int PORT = Integer.parseInt(System.getProperty("port", "8300"));
	
	public static final GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(
			GlobalEventExecutor.INSTANCE);
	
	public static final BidServerHandler bidServerHandler = new BidServerHandler();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ChannelFuture f;

	@PostConstruct
	public void start() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
		b.handler(new LoggingHandler());
		b.childHandler(new BidServerInitializer());
		b.option(ChannelOption.SO_BACKLOG, 1024);
		
		f = b.bind(PORT);
		
		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					if (logger.isErrorEnabled()) {
						logger.error("Socket bind failed.", future.cause());
					}
				}
			}
		});
	}

	@PreDestroy
	public void stop() throws Exception {
		if (!f.isDone()) {
			f.await();
		}
		f.channel().close().sync();
	}
}