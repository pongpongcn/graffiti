package com.alltobid.quotabid;

import java.util.Date;

public class ReceivedMessage {
	private final Message message;
	private final Date receiveTime;

	public Message getMessage() {
		return message;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public ReceivedMessage(Message message, Date receiveTime) {
		this.message = message;
		this.receiveTime = receiveTime;
	}

	@Override
	public String toString() {
		return String.format("message received at %tT", receiveTime);
	}
}
