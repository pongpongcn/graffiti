package io.netty.example.discard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Discards any incoming data.
 */
public class DiscardServer {

	private int port;

	public DiscardServer(int port) {
		this.port = port;
	}

	public void run() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1); // (1)
		EventLoopGroup workerGroup = new NioEventLoopGroup(8);
		try {
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // (3)
					.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							//ch.pipeline().addLast(new IdleStateHandler(10, 0, 0));
							ch.pipeline().addLast(new DiscardServerHandler(allChannels));
							ch.pipeline().addLast(globalTrafficShapingHandler);
						}
					}).option(ChannelOption.SO_BACKLOG, 128) // (5)
					.childOption(ChannelOption.SO_SNDBUF, 32768)
					.childOption(ChannelOption.SO_RCVBUF, 32768)
					.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 256)
					.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 1024);
					//.childOption(ChannelOption.WRITE_SPIN_COUNT, 2)

			ShowInfoThread showInfoThread = new ShowInfoThread();
			showInfoThread.start();
			BroadcastThread broadcastThread = new BroadcastThread();
			broadcastThread.start();

			// Bind and start to accept incoming connections.
			ChannelFuture f = b.bind(port).sync(); // (7)

			// Wait until the server socket is closed.
			// In this example, this does not happen, but you can do that to
			// gracefully
			// shut down your server.
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	private static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private static GlobalTrafficShapingHandler globalTrafficShapingHandler = new GlobalTrafficShapingHandler(
			GlobalEventExecutor.INSTANCE);

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		new DiscardServer(port).run();
	}

	public class ShowInfoThread extends Thread {
		@Override
		public void run() {
			try {
				while (true) {
					System.out.println("Client Count:" + allChannels.size() + ", Last Write Throughput:"
							+ String.format("%,d", globalTrafficShapingHandler.trafficCounter().lastWriteThroughput()));
					Thread.sleep(10000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public class BroadcastThread extends Thread {
		private String dataTemplate = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		@Override
		public void run() {
			int sn = 1;
			Calendar tempCurrentTime = Calendar.getInstance();
			tempCurrentTime.set(Calendar.MILLISECOND, 0);
			tempCurrentTime.add(Calendar.SECOND, 1);
			Calendar loopShoudEndTime = tempCurrentTime;

			try {
				while (true) {
					Calendar loopStartTime = Calendar.getInstance();

					boolean needBroadcast = allChannels.size() > 0;
					if (needBroadcast) {
						Date currentTime = loopStartTime.getTime();

						StringBuffer sb = new StringBuffer();
						sb.append(sdf.format(currentTime));
						sb.append(",");
						sb.append(Integer.toString(sn));
						sb.append(",");
						sb.append(dataTemplate.substring(0, 200 - sb.length()));
						allChannels.writeAndFlush(Unpooled.copiedBuffer(sb.toString().getBytes()),
								new ChannelMatcher() {
									@Override
									public boolean matches(Channel channel) {
										//对于发送缓冲区已满情况，则不发送广播消息。
										return channel.isWritable();
									}
								});
					}
					sn++;

					Calendar loopEndTime = Calendar.getInstance();

					if (loopEndTime.getTimeInMillis() <= loopShoudEndTime.getTimeInMillis()) {
						long sleepTime = (loopShoudEndTime.getTimeInMillis() - loopEndTime.getTimeInMillis()) + 1;
						Thread.sleep(sleepTime);
						loopShoudEndTime.add(Calendar.SECOND, 1);
					} else {
						loopShoudEndTime.add(Calendar.MILLISECOND,
								(int) (1000 + loopEndTime.getTimeInMillis() - loopShoudEndTime.getTimeInMillis()));
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}