package io.netty.example.http.helloworld;

import java.util.Base64;
import java.util.List;

import org.xxtea.XXTEA;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;

@Sharable
public class MessageEncrypter extends MessageToMessageEncoder<Message> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
		byte[] dataBytes = msg.getData().getBytes("UTF-8");
		
		dataBytes = XXTEA.encryptToBase64String(Base64.getEncoder().encode(dataBytes), Config.XXTEAKey).getBytes();
		
		String data = new String(dataBytes, "UTF-8");
		
		Message message = new Message(msg.getType(), msg.getSubType(), data);
		
		out.add(message);
	}

}
