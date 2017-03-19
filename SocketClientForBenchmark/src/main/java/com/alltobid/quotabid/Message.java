package com.alltobid.quotabid;

import java.util.Date;

public class Message {
	private final Date timestamp;
	private final int serialNumber;
	private final String data;

	public Date getTimestamp() {
		return timestamp;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public String getData() {
		return data;
	}

	public Message(Date timestamp, int serialNumber, String data) {
		this.timestamp = timestamp;
		this.serialNumber = serialNumber;
		this.data = data;
	}
}
