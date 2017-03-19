package com.alltobid.quotabid;

public class ShowInfoThread extends Thread {
	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("Client Count:" + BidLoadClient.bidLoadClientHandler.getChannelCount() + ", Last Read Throughput:"
						+ String.format("%,d", BidLoadClient.globalTrafficShapingHandler.trafficCounter().lastReadThroughput()));
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}