package com.alltobid.quotabid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShowInfoThread extends Thread {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void run() {
		try {
			long lastChannelInactiveTimes = 0;
			while (true) {
				long currentChannelInactiveTimes = SocketServer.bidServerHandler.getChannelInactiveTimes();
				if (logger.isInfoEnabled()) {
					logger.info("Client Count: {}, Last Disconnect Times: {}, Last Write Throughput: {}",
							SocketServer.bidServerHandler.getChannelCount(),
							currentChannelInactiveTimes - lastChannelInactiveTimes, String.format("%,d",
									SocketServer.globalTrafficShapingHandler.trafficCounter().lastWriteThroughput()));
				}
				lastChannelInactiveTimes = currentChannelInactiveTimes;
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}