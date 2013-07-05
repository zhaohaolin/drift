/*
 * Copyright 2013 The Drift Project
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
package org.driftframework.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.driftframework.endpoint.Endpoint;
import org.driftframework.endpoint.EndpointRepository;
import org.driftframework.util.TransportUtil;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientChannelHandler extends SimpleChannelHandler {
	
	private final static Logger			LOG		= LoggerFactory
														.getLogger(ClientChannelHandler.class);
	private ScheduledExecutorService	exec	= Executors
														.newSingleThreadScheduledExecutor();
	private EndpointRepository			endpointRepository;
	
	public ClientChannelHandler(EndpointRepository endpointRepository) {
		this.endpointRepository = endpointRepository;
	}
	
	/**
	 * 接收服务端返回消息时,取得注册在Channel上的Endpoint,然后执行Endpoint的消息接收函数
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		final Channel channel = e.getChannel();
		final Object msg = e.getMessage();
		if (LOG.isTraceEnabled()) {
			LOG.trace("messageReceived: [{}] at channel=[{}]", msg, channel);
		}
		
		Endpoint endpoint = TransportUtil.getEndpointOfChannel(channel);
		if (null != endpoint) {
			endpoint.messageReceived(TransportUtil.attachSender(msg, endpoint));
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn(
						"missing endpoint, ignore incoming msg: [{}], at channel=[{}]",
						e.getMessage(), channel);
			}
		}
	}
	
	/**
	 * 客户端通道打开时，暂时没有事件
	 */
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		final Channel channel = e.getChannel();
		if (LOG.isInfoEnabled()) {
			LOG.info("open channel: [{}]" + channel);
		}
	}
	
	/**
	 * 客户端通道关闭时，移出注册在通道上的Endpoint
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		final Channel channel = e.getChannel();
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				if (LOG.isDebugEnabled()) {
					LOG.debug(
							"channelClosed: remove channel id=[{}] at channel=[{}] ok.",
							channel.getId(), channel);
				}
				Endpoint endpoint = TransportUtil.getEndpointOfChannel(channel);
				if (null != endpoint) {
					endpoint.stop();
				}
				endpointRepository.removeEndpoint(endpoint);
				if (LOG.isDebugEnabled()) {
					LOG.debug("channelClosed: id=[{}] at channel=[{}] closed.",
							channel.getId(), channel);
				}
			}
			
		});
	}
	
	/**
	 * 异常发生时
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		final Channel channel = e.getChannel();
		if (LOG.isErrorEnabled())
			LOG.error("transport: [{}] at channel=[{}]", e.getCause(), channel);
		// 解码有错误的情况下，channel不关闭
		// e.getChannel().close();
	}
	
}
