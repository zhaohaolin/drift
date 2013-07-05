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

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.driftframework.cache.Holder;
import org.driftframework.codec.XipDecoder;
import org.driftframework.codec.XipEncoder;
import org.driftframework.context.Context;
import org.driftframework.endpoint.DefaultEndpointFactory;
import org.driftframework.endpoint.DefaultEndpointRepository;
import org.driftframework.endpoint.Endpoint;
import org.driftframework.endpoint.EndpointFactory;
import org.driftframework.endpoint.EndpointRepository;
import org.driftframework.protocol.Xip;
import org.driftframework.protocol.XipRequest;
import org.driftframework.protocol.XipResponse;
import org.driftframework.receiver.Receiver;
import org.driftframework.response.ResponseClosure;
import org.driftframework.session.AddressProvider;
import org.driftframework.session.DefaultAddressProvider;
import org.driftframework.util.TransportUtil;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
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
 * @version $Id: DefaultConnector, v 0.1 2013-7-1 上午12:06:19 Exp $
 */
public class DefaultConnector implements Connector {
	
	private final static Logger			LOG					= LoggerFactory
																	.getLogger(DefaultConnector.class);
	private ScheduledExecutorService	exec				= Executors
																	.newSingleThreadScheduledExecutor();
	private String						destIp				= "127.0.0.1";
	private int							destPort			= 0;
	private ClientBootstrap				client				= null;
	private List<String>				options;
	private long						reconnectTimeout	= 1;
	
	private ScheduledFuture<?>			lunchConnectFuture	= null;
	private ChannelFuture				connectFuture		= null;
	private AddressProvider				addressProvider		= null;
	
	private EndpointFactory				endpointFactory		= new DefaultEndpointFactory();
	private EndpointRepository			endpointRepository	= new DefaultEndpointRepository();
	
	/**
	 * the drift Context for Application
	 */
	private Context						context;
	private InternalLoggerFactory		loggerFactory		= new Slf4JLoggerFactory();
	
	public DefaultConnector() {
		//
	}
	
	public void start() {
		try {
			// register loggerFactory
			InternalLoggerFactory.setDefaultFactory(loggerFactory);
			
			addressProvider = new DefaultAddressProvider(destIp, destPort);
			
			client = new ClientBootstrap(new NioClientSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool()));
			
			// set codeFactory
			client.setPipelineFactory(new ChannelPipelineFactory() {
				
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					ChannelPipeline pipeline = Channels.pipeline();
					
					pipeline.addLast("clientHandler", new ClientChannelHandler(
							endpointRepository));
					pipeline.addLast("timeout", new IdleStateHandler(
							new HashedWheelTimer(), 10, 10, 0));
					pipeline.addLast("heartbeat", new HeartBeatHandler());
					
					pipeline.addLast("encoder", new XipEncoder() {
						{
							setProvider(context.getXipCodecProvider());
						}
					});
					
					pipeline.addLast("decoder", new XipDecoder() {
						{
							setProvider(context.getXipCodecProvider());
						}
					});
					
					return pipeline;
				}
				
			});
			
			// set options
			if (null != options && options.size() > 0) {
				for (String option : options) {
					client.setOption(option, true);
				}
			}
			
		} catch (Exception e) {
			if (LOG.isWarnEnabled()) {
				LOG.warn("[{}]", e);
			}
		} finally {
			//
		}// end try catch
		
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				doConnect();
			}
			
		});
		
	}
	
	public void stop() {
		if (null != client) {
			client.shutdown();
			client.releaseExternalResources();
			client = null;
		}
		
		exec.shutdownNow();
	}
	
	private void doScheduleNextConnect() {
		if (null == lunchConnectFuture || lunchConnectFuture.isDone()) {
			lunchConnectFuture = exec.schedule(new Runnable() {
				@Override
				public void run() {
					try {
						doConnect();
					} catch (Exception e) {
						LOG.error("[{}]" + e);
					}
				}
			}, reconnectTimeout, TimeUnit.SECONDS);
			
			if (LOG.isTraceEnabled()) {
				LOG.trace("doScheduleNextConnect: next connect scheduled");
			}
		} else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("doScheduleNextConnect: next connect !NOT! scheduled.");
			}
		}
	}
	
	private void doConnect() {
		
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				doScheduleNextConnect();
			}
			
		});
		
		if (endpointRepository.isFull()) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("doConnect: reach max channel: "
						+ endpointRepository.getMaxSession()
						+ ", cancel this action.");
			}
			return;
		}
		
		if (null != connectFuture && !connectFuture.isDone()) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("doConnect: connect already doing, cancel this action.");
			}
			return;
		}
		
		if (null == addressProvider) {
			LOG.error("address provider is null.");
			return;
		}
		
		if (LOG.isInfoEnabled()) {
			LOG.info("start connect using address provider");
		}
		
		InetSocketAddress addr = addressProvider.getAddress();
		if (null != addr) {
			if (LOG.isInfoEnabled()) {
				LOG.info("start connect [{}:{}]", addr.getAddress()
						.getHostAddress(), addr.getPort());
			}
			try {
				// 连接Socket
				connectFuture = client.connect(addr);
			} catch (Exception e) {
				LOG.error("[{}]" + e);
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("failed to using address provider get address");
			}
			return;
		}
		
		// 监听连接事件
		connectFuture.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(final ChannelFuture connectFuture) {
				// 连接完成后
				exec.submit(new Runnable() {
					
					@Override
					public void run() {
						onConnectComplete(connectFuture);
					}
					
				});
			}
		});
	}
	
	// 连接完成后要处理的业务
	private void onConnectComplete(ChannelFuture future) {
		Channel channel = future.getChannel();
		if (channel.isOpen()) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("onConnectComplete: channel [" + channel
						+ "] connected.");
			}
			
			// 创建endpoint并注册
			Endpoint endpoint = endpointFactory.createEndpoint(channel);
			TransportUtil.addEndpointToChannel(channel, endpoint);
			endpointRepository.addEndpoint(endpoint);
			
		} else {
			// not connected
			LOG.error("onConnectComplete: channel [" + channel
					+ "] connect failed.");
			if (null != connectFuture) {
				connectFuture = null;
			}
		}
	}
	
	public String getDestIp() {
		return destIp;
	}
	
	public void setDestIp(String destIp) {
		this.destIp = destIp;
	}
	
	public int getDestPort() {
		return destPort;
	}
	
	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	
	public void setOptions(List<String> options) {
		this.options = options;
	}
	
	public Context getContext() {
		return context;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setLoggerFactory(InternalLoggerFactory loggerFactory) {
		this.loggerFactory = loggerFactory;
	}
	
	public void setReconnectTimeout(long reconnectTimeout) {
		this.reconnectTimeout = reconnectTimeout;
	}
	
	public void setEndpointRepository(EndpointRepository endpointRepository) {
		this.endpointRepository = endpointRepository;
	}
	
	public void setEndpointFactory(EndpointFactory endpointFactory) {
		this.endpointFactory = endpointFactory;
	}
	
	public void setNextClosure(Closure nextClosure) {
		this.endpointFactory.setNextClosure(nextClosure);
	}
	
	public void setReceiver(Receiver receiver) {
		this.endpointFactory.setReceiver(receiver);
	}
	
	public void setCachedMessageCount(int cachedMessageCount) {
		this.endpointFactory.setCachedMessageCount(cachedMessageCount);
	}
	
	public void setMaxSession(int maxSession) {
		this.endpointRepository.setMaxSession(maxSession);
	}
	
	public <Req extends Xip> void send(Req req) {
		Endpoint endpoint = endpointRepository.getEndpoint();
		if (null != endpoint)
			endpoint.send(req);
	}
	
	public <Req extends XipRequest, Resp extends XipResponse> void send(
			Req req, ResponseClosure<Resp> callback) {
		Endpoint endpoint = endpointRepository.getEndpoint();
		if (endpoint != null) {
			endpoint.send(req, callback);
		}
	}
	
	public <Req extends XipRequest, Resp extends XipResponse> Resp sendAndWait(
			Req req) {
		Endpoint endpoint = endpointRepository.getEndpoint();
		if (endpoint != null) {
			return endpoint.sendAndWait(req);
		}
		return null;
	}
	
	public <Req extends Xip, Resp extends XipResponse> Resp sendAndWait(
			Req req, long timeout, TimeUnit units) {
		Endpoint endpoint = endpointRepository.getEndpoint();
		if (endpoint != null) {
			return endpoint.sendAndWait(req, timeout, units);
		}
		return null;
	}
	
	@Override
	public void setResponseContext(Holder<String, Object> responseContext) {
		this.endpointFactory.setResponseContext(responseContext);
	}
	
	@Override
	public <Req extends Xip> void send(String path, Req req) {
		req.setPath(path);
		send(req);
	}
	
	@Override
	public <Req extends XipRequest, Resp extends XipResponse> void send(
			String path, Req req, ResponseClosure<Resp> callback) {
		req.setPath(path);
		send(req, callback);
	}
	
	@Override
	public <Req extends XipRequest, Resp extends XipResponse> Resp sendAndWait(
			String path, Req req) {
		req.setPath(path);
		return sendAndWait(req);
	}
	
	@Override
	public <Req extends Xip, Resp extends XipResponse> Resp sendAndWait(
			String path, Req req, long timeout, TimeUnit units) {
		req.setPath(path);
		return sendAndWait(req, timeout, units);
	}
	
}
