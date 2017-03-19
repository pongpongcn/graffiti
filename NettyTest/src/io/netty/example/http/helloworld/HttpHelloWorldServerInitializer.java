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
package io.netty.example.http.helloworld;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;

public class HttpHelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;
	
	private static final LengthFieldPrepender lengthFieldPrepender = new LengthFieldPrepender(4, true);
	private static final MessageEncoder messageEncoder = new MessageEncoder();
	private static final MessageDecrypter messageDecrypter = new MessageDecrypter();
	private static final MessageEncrypter messageEncrypter = new MessageEncrypter();
	private static final LoggingHandler loggingHandler = new LoggingHandler();
	private static final HttpHelloWorldServerHandler httpHelloWorldServerHandler = new HttpHelloWorldServerHandler();

	public HttpHelloWorldServerInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();

		p.addLast(new IdleStateHandler(Config.HeartBeatInterval, 0, 0));
		
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		
		p.addLast(new LengthFieldBasedFrameDecoder(4096, 0, 4, -4, 4));
		p.addLast(lengthFieldPrepender);

		p.addLast(new MessageDecoder());
		p.addLast(messageEncoder);

		p.addLast(messageDecrypter);
		p.addLast(messageEncrypter);
		
		p.addLast(loggingHandler);

		p.addLast(httpHelloWorldServerHandler);
	}
}
