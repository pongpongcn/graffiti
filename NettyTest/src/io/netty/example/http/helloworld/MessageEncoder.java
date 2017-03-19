package io.netty.example.http.helloworld;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
public class MessageEncoder extends MessageToByteEncoder<Message> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
		out.writeByte(msg.getType());
		out.writeByte(msg.getSubType());

		byte[] dataBytes = msg.getData().getBytes("UTF-8");
		
		int dataBytesLength = dataBytes.length;
		out.writeInt(dataBytesLength);
		out.writeBytes(dataBytes);
	}
}
