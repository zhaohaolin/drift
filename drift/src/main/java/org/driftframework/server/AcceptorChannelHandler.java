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
package org.driftframework.server;

import org.driftframework.endpoint.Endpoint;
import org.driftframework.endpoint.EndpointFactory;
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

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: AcceptorIOHandler, v 0.1 2013年7月2日 下午4:05:14 Exp $
 */
public class AcceptorChannelHandler extends SimpleChannelHandler {
	
	private final static Logger		LOG	= LoggerFactory
												.getLogger(AcceptorChannelHandler.class);
	private final EndpointFactory	endpointFactory;
	
	public AcceptorChannelHandler(EndpointFactory endpointFactory) {
		super();
		this.endpointFactory = endpointFactory;
	}
	
	// 被连接上时
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// 注册endpoint
		Endpoint endpoint = endpointFactory.createEndpoint(e.getChannel());
		if (null != endpoint) {
			TransportUtil.addEndpointToChannel(e.getChannel(), endpoint);
		}
	}
	
	// 被开启时
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("channelOpened: channel [" + e.getChannel() + "]");
		}
	}
	
	// 接收消息时：
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		// 已经经过解码后的对象
		final AbstractXip msg = (AbstractXip) e.getMessage();
		if (LOG.isTraceEnabled()) {
			LOG.trace("messageReceived: " + msg);
		}
		
		// 从channel取得endpoint,并处理业务
		Endpoint endpoint = TransportUtil.getEndpointOfChannel(e.getChannel());
		if (null != endpoint) {
			endpoint.messageReceived(TransportUtil.attachSender(msg, endpoint));
		} else {
			LOG.warn("missing endpoint, ignore incoming msg:", msg);
		}
	}
	
	// 关闭时
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// 清除不用的资源占用
		Channel channel = e.getChannel();
		if (LOG.isDebugEnabled()) {
			LOG.debug("channel: " + channel.getId());
		}
		Endpoint endpoint = TransportUtil.getEndpointOfChannel(channel);
		if (null != endpoint) {
			endpoint.stop();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.warn("Unexpected exception from downstream." + e.getCause());
		// 解码有错误的情况下，session不关闭
		// e.getChannel().close();
	}
	
}
