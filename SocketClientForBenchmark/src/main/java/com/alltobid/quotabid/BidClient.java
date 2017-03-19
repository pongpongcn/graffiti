/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.alltobid.quotabid;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Sends one message when a connection is open and echoes back any received data
 * to the server. Simply put, the echo client initiates the ping-pong traffic
 * between the echo client and server by sending the first message to the
 * server.
 */
public final class BidClient {

	private static final String HOST = System.getProperty("host", "localhost");
	private static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
	
	private static final Logger logger = LoggerFactory.getLogger(BidClient.class);

	public static final GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(
			GlobalEventExecutor.INSTANCE);

	public static final ConcurrentLinkedQueue<ReceivedMessage> receivedMessageQueue = new ConcurrentLinkedQueue<ReceivedMessage>();
	public static final BidClientHandler bidClientHandler = new BidClientHandler();

	public static void main(String[] args) throws Exception {
		bidClientHandler.addListener(new BidClientHandlerAdapter() {
			@Override
			public void messageReceived(ReceivedMessage receivedMessage) {
				receivedMessageQueue.add(receivedMessage);
			}
		});

		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(new BidClientInitializer());

			MonitorThread monitorThread = new MonitorThread();
			monitorThread.setName("BidClient Monitor");
			monitorThread.start();

			while (true) {
				try {
					// Start the client.
					Channel ch = b.connect(HOST, PORT).sync().channel();

					// Wait until the connection is closed.
					ch.closeFuture().sync();
				} catch (Exception ex) {
					logger.error("Channel Error", ex);
					Thread.sleep(5000);
				}
			}
		} finally {
			// Shut down the event loop to terminate all threads.
			group.shutdownGracefully();
		}
	}
}
