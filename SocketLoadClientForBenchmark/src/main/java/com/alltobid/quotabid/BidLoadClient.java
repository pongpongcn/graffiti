package com.alltobid.quotabid;

import java.util.concurrent.Semaphore;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class BidLoadClient {
	private static final String HOST = System.getProperty("host", "localhost");
	private static final int PORT = Integer.parseInt(System.getProperty("port", "8300"));
	private static final Semaphore maxClients = new Semaphore(
			Integer.parseInt(System.getProperty("maxClientCount", "10000")));
	private static final Semaphore maxConnectingClients = new Semaphore(
			Integer.parseInt(System.getProperty("maxConnectingClientCount", "10")));
	public static final GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(
			GlobalEventExecutor.INSTANCE);
	public static final BidLoadClientHandler bidLoadClientHandler = new BidLoadClientHandler();
	public static final StringEncoder stringEncoder = new StringEncoder(CharsetUtil.UTF_8);

	public void connect(String host, int port, EventLoopGroup group, BidLoadClientHandler bidLoadClientHandler) {
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();

				p.addLast(new IdleStateHandler(30, 10, 0));
				p.addLast(BidLoadClient.globalTrafficShapingHandler);
				p.addLast(stringEncoder);
				p.addLast(bidLoadClientHandler);
			}
		});

		try {
			maxConnectingClients.acquire();
			Channel ch = b.connect(host, port).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					maxConnectingClients.release();
				}
			}).channel();

			ch.closeFuture().addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					maxClients.release();
				}
			});

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		EventLoopGroup group = new NioEventLoopGroup();

		ShowInfoThread showInfoThread = new ShowInfoThread();
		showInfoThread.setName("BidLoadClient ShowInfo");
		showInfoThread.start();

		while (true) {
			try {
				maxClients.acquire();
				BidLoadClient client = new BidLoadClient();
				client.connect(HOST, PORT, group, bidLoadClientHandler);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}

		group.shutdownGracefully();
	}

}
