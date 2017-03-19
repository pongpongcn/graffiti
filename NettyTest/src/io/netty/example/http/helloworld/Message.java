package io.netty.example.http.helloworld;

public class Message {
	private final byte type;
	private final byte subType;
	private final String data;

	public byte getType() {
		return type;
	}

	public byte getSubType() {
		return subType;
	}

	public String getData() {
		return data;
	}

	public Message(byte type, byte subType, String data) {
		this.type = type;
		this.subType = subType;
		this.data = data;
	}

	@Override
	public String toString() {
		return String.format("%d-%d, %s", type, subType, data);
	}
}
