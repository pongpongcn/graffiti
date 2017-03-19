package io.netty.example.http.helloworld;

import java.util.Base64;
import java.util.List;

import org.xxtea.XXTEA;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

@Sharable
public class MessageDecrypter extends MessageToMessageDecoder<Message> {

	@Override
	protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		byte[] dataBytes = msg.getData().getBytes("UTF-8");
		
		dataBytes = Base64.getDecoder().decode(XXTEA.decryptBase64String(new String(dataBytes, "UTF-8"), Config.XXTEAKey));
		
		String data = new String(dataBytes, "UTF-8");
		
		Message message = new Message(msg.getType(), msg.getSubType(), data);
		
		out.add(message);
	}

}
