package com.alltobid.quotabid;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorThread extends Thread {
	private final Logger logger = LoggerFactory.getLogger(MonitorThread.class);

	private Duration bestLatency;
	private Duration lastestDeliverLatency;
	private Duration lastestSendTimeSpan;
	private ReceivedMessage lastReceivedMessage;
	private int totalReceivedMessageCount;
	private Map<Integer, Integer> badDeliverLatencyMessageCounts;
	private Map<Integer, Integer> badSendTimeSpanMessageCounts;
	private int discontinuousSNMessageCount;
	private int interruptCount;

	@Override
	public void run() {
		resetStat();

		try {
			while (true) {
				ReceivedMessage receivedMessage = BidClient.receivedMessageQueue.poll();
				if (receivedMessage != null) {
					stat(receivedMessage);
				} else {
					outputStat();
					Thread.sleep(5000);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void outputStat() {
		StringBuffer sb = new StringBuffer();
		if (lastestDeliverLatency != null) {
			sb.append(String.format("Lastest Deliver Latency: %.2fs%n",
					(double) lastestDeliverLatency.toMillis() / 1000));
		} else {
			sb.append(String.format("Lastest Deliver Latency: N/A%n"));
		}
		if (lastestSendTimeSpan != null) {
			sb.append(String.format("Lastest Send Time Span: %.2fs%n", (double) lastestSendTimeSpan.toMillis() / 1000));
		} else {
			sb.append(String.format("Lastest Send Time Span: N/A%n"));
		}
		sb.append(String.format("Total Received Message Count: %d%n", totalReceivedMessageCount));
		sb.append(String.format("Discontinuous SN Message Count: %d%n", discontinuousSNMessageCount));
		sb.append(String.format("Interrupt Count: %d%n", interruptCount));
		sb.append(String.format("Bad Deliver Latency Message Counts:%n"));
		Integer[] badDeliverLatencyMessageCountKeys = badDeliverLatencyMessageCounts.keySet().toArray(new Integer[0]);
		Arrays.sort(badDeliverLatencyMessageCountKeys);
		for (Integer key : badDeliverLatencyMessageCountKeys) {
			if (key >= 5) {
				sb.append(String.format("    %ds and above: %d%n", key, badDeliverLatencyMessageCounts.get(key)));
			} else {
				sb.append(String.format("    %ds: %d%n", key, badDeliverLatencyMessageCounts.get(key)));
			}
		}

		sb.append(String.format("Bad Send Time Span Message Counts:%n"));
		Integer[] badSendTimeSpanMessageCountKeys = badSendTimeSpanMessageCounts.keySet().toArray(new Integer[0]);
		Arrays.sort(badSendTimeSpanMessageCountKeys);
		for (Integer key : badSendTimeSpanMessageCountKeys) {
			if (key >= 5) {
				sb.append(String.format("    %ds and above: %d%n", key, badSendTimeSpanMessageCounts.get(key)));
			} else {
				sb.append(String.format("    %ds: %d%n", key, badSendTimeSpanMessageCounts.get(key)));
			}
		}

		String logText = sb.toString();

		System.out.println(logText);
	}

	private void resetStat() {
		bestLatency = null;
		lastReceivedMessage = null;

		lastestDeliverLatency = null;
		lastestSendTimeSpan = null;
		totalReceivedMessageCount = 0;
		badDeliverLatencyMessageCounts = new HashMap<Integer, Integer>();
		badSendTimeSpanMessageCounts = new HashMap<Integer, Integer>();
		discontinuousSNMessageCount = 0;
		interruptCount = 0;
	}

	private void stat(ReceivedMessage receivedMessage) {
		if (receivedMessage.getMessage() == null) {
			bestLatency = null;
			lastestDeliverLatency = null;
			lastestSendTimeSpan = null;
			if (lastReceivedMessage != null) {
				interruptCount++;
			}
			lastReceivedMessage = null;
			return;
		}

		totalReceivedMessageCount++;

		// 表面上的延迟
		Duration simpleDelay = Duration.ofMillis(
				receivedMessage.getReceiveTime().getTime() - receivedMessage.getMessage().getTimestamp().getTime());
		if (bestLatency == null || simpleDelay.compareTo(bestLatency) < 0) {
			bestLatency = simpleDelay;
			logger.debug(String.format("ServerClientTimeOffset Updated: %sms", bestLatency.toMillis()));
		}

		Duration increasedDelay = simpleDelay.minus(bestLatency);
		logger.debug(String.format("increasedDelay: %sms", increasedDelay.toMillis()));

		lastestDeliverLatency = increasedDelay;

		if (increasedDelay.toMillis() > 1000) {
			Integer key = (int) increasedDelay.getSeconds();
			if (key > 5) {
				key = 5;// 超过5的归入5
			}
			if (badDeliverLatencyMessageCounts.containsKey(key)) {
				badDeliverLatencyMessageCounts.put(key, badDeliverLatencyMessageCounts.get(key) + 1);
			} else {
				badDeliverLatencyMessageCounts.put(key, 1);
			}
		}

		if (lastReceivedMessage != null) {
			if (receivedMessage.getMessage().getSerialNumber()
					- lastReceivedMessage.getMessage().getSerialNumber() > 1) {
				logger.debug(
						String.format("Discontinuous SN: %s, %s", lastReceivedMessage.getMessage().getSerialNumber(),
								receivedMessage.getMessage().getSerialNumber()));

				discontinuousSNMessageCount++;
			}

			Duration messageTimeSpan = Duration.ofMillis(receivedMessage.getMessage().getTimestamp().getTime()
					- lastReceivedMessage.getMessage().getTimestamp().getTime());
			logger.debug(String.format("messageTimeSpan: %sms", messageTimeSpan.toMillis()));

			lastestSendTimeSpan = messageTimeSpan;

			if (messageTimeSpan.toMillis() > 2000) {
				int key = (int) messageTimeSpan.getSeconds();
				if (key > 5) {
					key = 5;// 超过5的归入5
				}
				if (badSendTimeSpanMessageCounts.containsKey(key)) {
					badSendTimeSpanMessageCounts.put(key, badSendTimeSpanMessageCounts.get(key) + 1);
				} else {
					badSendTimeSpanMessageCounts.put(key, 1);
				}
			}
		}

		lastReceivedMessage = receivedMessage;
	}
}