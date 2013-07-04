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
import org.driftframework.protocol.AbstractXip;
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
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Channel channel = e.getChannel();
		final AbstractXip msg = (AbstractXip) e.getMessage();
		if (LOG.isTraceEnabled()) {
			LOG.trace("messageReceived: [{}]" + msg);
		}
		
		Endpoint endpoint = TransportUtil.getEndpointOfChannel(channel);
		if (null != endpoint) {
			endpoint.messageReceived(TransportUtil.attachSender(msg, endpoint));
		} else {
			LOG.warn("missing endpoint, ignore incoming msg: [{}]"
					+ e.getMessage());
		}
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("open channel: [{}]" + e.getChannel());
		}
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		final Channel channel = e.getChannel();
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				LOG.debug("channelClosed: remove " + channel.getId() + " ok.");
				Endpoint endpoint = TransportUtil.getEndpointOfChannel(channel);
				if (null != endpoint) {
					endpoint.stop();
				}
				endpointRepository.removeEndpoint(endpoint);
				LOG.info("channelClosed: " + channel.getId() + " closed.");
			}
			
		});
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("transport: [{}]", e);
		// 解码有错误的情况下，channel不关闭
		// e.getChannel().close();
	}
	
}
