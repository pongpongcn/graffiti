/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.alltobid.quotabid;

import java.util.concurrent.atomic.AtomicLong;

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
public class BidServerHandler extends ChannelDuplexHandler {
	private final ConcurrentSet<Channel> channels = new ConcurrentSet<Channel>();
	private final Logger logger = LoggerFactory.getLogger(BidServerHandler.class);
	private final AtomicLong channelInactiveTimes = new AtomicLong();

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
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		channelInactiveTimes.incrementAndGet();
		super.channelInactive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.debug("BidServerHandler caught an exception.", cause);
		ctx.close();
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				ctx.close();
			}
		}
	}

	public void BroadcastMessage(String message) {
		if (channels.isEmpty()) {
			return;
		}

		// 对于发送缓冲区已满的Channel，不发送广播消息。
		channels.parallelStream().forEach(c -> {
			if (c.isWritable()) {
				c.writeAndFlush(message, c.voidPromise());
			}
		});
	}

	public int getChannelCount() {
		return channels.size();
	}
	
	public long getChannelInactiveTimes(){
		return channelInactiveTimes.get();
	}
}
