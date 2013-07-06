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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import org.driftframework.cache.Holder;
import org.driftframework.codec.XipDecoder;
import org.driftframework.codec.XipEncoder;
import org.driftframework.context.Context;
import org.driftframework.endpoint.DefaultEndpointFactory;
import org.driftframework.endpoint.EndpointFactory;
import org.driftframework.receiver.Receiver;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolkit.lang.Closure;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultTCPAcceptor, v 0.1 2013-6-30 下午9:15:47 Exp $
 */
public class DefaultAcceptor implements Acceptor {
	
	private final static Logger		LOG				= LoggerFactory
															.getLogger(DefaultAcceptor.class);
	private String					ip				= "127.0.0.1";
	private int						port			= 7777;
	private int						maxRetryCount	= 20;
	private long					retryTimeout	= 30 * 1000;
	private ServerBootstrap			bootstrap		= null;
	private List<String>			options			= null;
	private String					name;
	private ChannelGroup			group			= null;
	
	/**
	 * the drift Context for Application
	 */
	private Context					context;
	private InternalLoggerFactory	loggerFactory	= new Slf4JLoggerFactory();
	private final EndpointFactory	endpointFactory	= new DefaultEndpointFactory();
	
	public DefaultAcceptor() {
		//
	}
	
	public void start() {
		// register logger factory
		InternalLoggerFactory.setDefaultFactory(loggerFactory);
		
		// group
		group = new DefaultChannelGroup(name);
		
		// new server socket bootstrap
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("init the tcp acceptor bossExecutor and workExecutor ok.");
		}
		
		ChannelPipeline pipeline = bootstrap.getPipeline();
		
		{
			pipeline.addLast("encoder",
					new XipEncoder(context.getXipCodecProvider()));
			pipeline.addLast("decoder",
					new XipDecoder(context.getXipCodecProvider()));
			
			pipeline.addLast("acceptorHandler", new AcceptorChannelHandler(
					endpointFactory));
			pipeline.addLast("timeout", new IdleStateHandler(
					new HashedWheelTimer(), 10, 10, 0));
			pipeline.addLast("heartbeat", new HeartBeatHandler());
		}
		
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setOption("reuseAddress", true);
		
		// set options
		if (null != options && options.size() > 0) {
			for (String option : options) {
				bootstrap.setOption(option, true);
			}
		}
		
		int retryCount = 0;
		boolean binded = false;
		do {
			try {
				Channel channel = bootstrap.bind(new InetSocketAddress(this.ip,
						this.port));
				binded = true;
				group.add(channel);
				
			} catch (Exception e) {
				LOG.warn("start failed on port:[{}], " + e + ", and retry...",
						port);
				// 对绑定异常再次进行尝试
				retryCount++;
				if (retryCount >= maxRetryCount) {
					// 超过最大尝试次数
					throw new RuntimeException(e);
				}
				try {
					Thread.sleep(retryTimeout);
				} catch (InterruptedException e1) {
					if (LOG.isErrorEnabled()) {
						LOG.error("[{}]", e1.fillInStackTrace());
					}
				}
			}
		} while (!binded);
		
		if (LOG.isInfoEnabled()) {
			LOG.info("start succeed in [{}]:[{}]", new Object[] { ip, port });
		}
	}
	
	public void stop() {
		ChannelGroupFuture future = group.close();
		future.awaitUninterruptibly();// 阻塞直到服务器关闭
		
		if (null != bootstrap) {
			bootstrap.shutdown();// 停止，但未运行完成的事务将继续执行
			bootstrap.releaseExternalResources();
			bootstrap = null;
		}
	}
	
	@Override
	public void setContext(Context ctx) {
		this.context = ctx;
	}
	
	@Override
	public Context getContext() {
		return this.context;
	}
	
	@Override
	public void setExportIp(String ip) {
		this.ip = ip;
	}
	
	@Override
	public void setExportPort(int port) {
		this.port = port;
	}
	
	@Override
	public void setLoggerFactory(InternalLoggerFactory loggerFactory) {
		this.loggerFactory = loggerFactory;
	}
	
	@Override
	public void setOptions(List<String> options) {
		this.options = options;
	}
	
	@Override
	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}
	
	@Override
	public void setRetryTimeout(long retryTimeout) {
		this.retryTimeout = retryTimeout;
	}
	
	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public void setNextClosure(Closure nextClosure) {
		endpointFactory.setNextClosure(nextClosure);
	}
	
	public void setReceiver(Receiver receiver) {
		endpointFactory.setReceiver(receiver);
	}
	
	public void setCachedMessageCount(int cachedMessageCount) {
		endpointFactory.setCachedMessageCount(cachedMessageCount);
	}
	
	public void setResponseContext(Holder<String, Object> responseContext) {
		endpointFactory.setResponseContext(responseContext);
	}
	
}
