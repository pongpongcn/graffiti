package com.alltobid.quotabid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ConcurrentSet;

@Sharable
public class BidLoadClientHandler extends ChannelDuplexHandler {
	private final Logger logger = LoggerFactory.getLogger(BidLoadClientHandler.class);
	private final ConcurrentSet<Channel> channels = new ConcurrentSet<Channel>();
	private final String heartBeat = "BEAT";

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ReferenceCountUtil.release(msg);
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		channels.add(ctx.channel());
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		channels.remove(ctx.channel());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.debug("BidLoadClientHandler caught an exception.", cause);
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				ctx.close();
			}
			else if(e.state() == IdleState.WRITER_IDLE) {
				ctx.writeAndFlush(heartBeat);
			}
		}
	}
	
	public int getChannelCount() {
		return channels.size();
	}
}
