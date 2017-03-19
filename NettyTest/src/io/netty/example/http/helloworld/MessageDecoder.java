package io.netty.example.http.helloworld;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		byte type = in.readByte();
		byte subType = in.readByte();
		
		int dataBytesLength = in.readInt();
		
		byte[] dataBytes = new byte[dataBytesLength];
		in.readBytes(dataBytes);

		String data = new String(dataBytes, "UTF-8");
		
		Message message = new Message(type, subType, data);
		
		out.add(message);
	}

}
