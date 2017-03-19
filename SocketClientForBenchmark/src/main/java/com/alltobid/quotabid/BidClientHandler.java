/*
 * Copyright 2012 The Netty Project
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class BidClientHandler extends ChannelInboundHandlerAdapter {
	private final Logger logger = LoggerFactory.getLogger(BidClientHandler.class);
	private final List<BidClientHandlerListener> listeners = new ArrayList<BidClientHandlerListener>();
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		Date receiveTime = Calendar.getInstance().getTime();

		String[] msgParts = ((String) msg).split(",");

		Date timestamp;
		try {
			timestamp = sdf.parse(msgParts[0]);
		} catch (ParseException ex) {
			logger.error("Parse timestamp failed.", ex);
			timestamp = null;
		}

		Message message = new Message(timestamp, Integer.parseInt(msgParts[1]), msgParts[2]);

		ReceivedMessage receivedMessage = new ReceivedMessage(message, receiveTime);
		onMessageReceived(receivedMessage);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Date receiveTime = Calendar.getInstance().getTime();
		Message message = null;

		ReceivedMessage receivedMessage = new ReceivedMessage(message, receiveTime);
		onMessageReceived(receivedMessage);
	}

	private void onMessageReceived(ReceivedMessage receivedMessage) {
		for (BidClientHandlerListener listener : listeners)
			listener.messageReceived(receivedMessage);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.debug("BidClientHandler caught an exception.", cause);
		ctx.close();
	}

	public void addListener(BidClientHandlerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(BidClientHandlerListener listener) {
		listeners.remove(listener);
	}
}
