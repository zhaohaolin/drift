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
package org.driftframework.endpoint;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.driftframework.cache.DefaultHolder;
import org.driftframework.cache.Holder;
import org.driftframework.protocol.Xip;
import org.driftframework.protocol.XipRequest;
import org.driftframework.protocol.XipResponse;
import org.driftframework.receiver.Receiver;
import org.driftframework.response.ResponseClosure;
import org.driftframework.response.ResponseFuture;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolkit.lang.Closure;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultEndpoint, v 0.1 2013年7月2日 下午10:57:04 Exp $
 */
public class DefaultEndpoint implements Endpoint {
	
	private final static Logger						LOG				= LoggerFactory
																			.getLogger(DefaultEndpoint.class);
	protected Closure								nextClosure		= null;
	protected Receiver								receiver		= null;
	private AtomicReference<Holder<String, Object>>	responseContext	= new AtomicReference<Holder<String, Object>>();
	protected BlockingQueue<Xip>					pendings		= new LinkedBlockingQueue<Xip>(
																			1024);
	private Channel									channel			= null;
	private ExecutorService							exec			= Executors
																			.newSingleThreadExecutor();
	private long									waitTimeout		= 1;												// 等待超时时长
	private int										sendTimeout		= 10000;											// 发送超时时长
																														
	private void addToPending(final Xip msg) {
		if (null != msg) {
			while (!pendings.offer(msg)) {
				if (LOG.isInfoEnabled())
					LOG.info("addToPending: offer msg to cache failed, try remove early cached msg.");
				pendings.poll();
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void doSend() {
		while (true) {
			Xip msg = null;
			try {
				msg = pendings.take();
			} catch (InterruptedException e) {
				LOG.error("get message: [{}]", e);
				return;
			}
			
			if (null != msg) {
				ChannelFuture future = channel.write(msg);
				future.addListener(new ChannelFutureListener() {
					
					@Override
					public void operationComplete(ChannelFuture future)
							throws Exception {
						if (!future.isSuccess()) {
							// TODO 失败的消息是否重发
							// addToPending(msg);
							if (future.isCancelled()) {
								LOG.error("send msg failed, reason: [{}]",
										future.getCause().getMessage());
							} else {
								LOG.error("send msg failed without reason.");
							}
						}
					}
					
				});
			}
		}
	}
	
	private void doSendPending() {
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				sendPending();
			}
			
		});
	}
	
	private void sendPending() {
		try {
			if (null == channel) {
				Thread.sleep(waitTimeout);// sleep
			} else {
				final Xip msg = pendings.poll(waitTimeout, TimeUnit.SECONDS);
				if (null != msg) {
					ChannelFuture future = channel.write(msg);
					future.addListener(new ChannelFutureListener() {
						
						@Override
						public void operationComplete(ChannelFuture future)
								throws Exception {
							if (!future.isSuccess()) {
								// TODO 失败的消息是否重发
								// addToPending(msg);
								if (future.isCancelled()) {
									LOG.error("send msg failed, reason: [{}]",
											future.getCause().getMessage());
								} else {
									LOG.error("send msg failed without reason");
								}
							}
						}
						
					});
				}
			}
		} catch (InterruptedException e) {
			// TODO: handle exception
		} finally {
			doSendPending();
		}
	}
	
	@Override
	public void start() {
		exec.submit(new Runnable() {
			
			@Override
			public void run() {
				doSendPending();
			}
			
		});
	}
	
	@Override
	public void stop() {
		this.exec.shutdownNow();
		this.pendings.clear();
		this.responseContext = null;
		this.nextClosure = null;
		this.receiver = null;
		this.channel = null;
	}
	
	@Override
	public void setQueueSize(int cachedMessageCount) {
		this.pendings = new LinkedBlockingQueue<Xip>(cachedMessageCount);
	}
	
	@Override
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	@Override
	public void setNextClosure(Closure nextClosure) {
		this.nextClosure = nextClosure;
	}
	
	@Override
	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}
	
	@Override
	public void setResponseContext(Holder<String, Object> context) {
		this.responseContext.set(context);
	}
	
	public int getPendingCount() {
		if (null != this.pendings) {
			return this.pendings.size();
		}
		return -1;
	}
	
	public Holder<String, Object> getResponseContext() {
		Holder<String, Object> ret = responseContext.get();
		if (ret == null) {
			ret = new DefaultHolder<String, Object>();
			responseContext.set(ret);
		}
		return ret;
	}
	
	public void setExec(ExecutorService exec) {
		this.exec = exec;
	}
	
	public void setWaitTimeout(long waitTimeout) {
		this.waitTimeout = waitTimeout;
	}
	
	public void setSendTimeout(int sendTimeout) {
		this.sendTimeout = sendTimeout;
	}
	
	// receiver
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void messageReceived(final Object input) {
		final Xip msg = (Xip) input;
		String uuid = msg.getIdentification();
		Object obj = getResponseContext().getAndRemove(uuid);
		if (null != obj) {
			try {
				if (obj instanceof ResponseFuture) {
					((ResponseFuture) obj).set(msg);
				}
				if (obj instanceof ResponseClosure) {
					((ResponseClosure) obj).onResponse(msg);
				}
			} catch (Exception e) {
				LOG.error("onResponse error.", e);
			}
		} else {
			if (this.receiver != null) {
				this.receiver.messageReceived(msg);
			}
		}
		
		if (null != nextClosure) {
			this.nextClosure.execute(msg);
		}
	}
	
	// send
	public <Req extends Xip> void send(final Req req) {
		if (null != req) {
			addToPending(req);
		}
	}
	
	public <Req extends XipRequest, Resp extends XipResponse> void send(
			Req req, ResponseClosure<Resp> callback) {
		if (null != req) {
			String uuid = req.getIdentification();
			getResponseContext().put(uuid, callback);
			addToPending(req);
		}
	}
	
	public <Req extends XipRequest, Resp extends XipResponse> Resp sendAndWait(
			Req req) {
		return sendAndWait(req, sendTimeout, TimeUnit.MILLISECONDS);
	}
	
	@SuppressWarnings("unchecked")
	public <Req extends Xip, Resp extends XipResponse> Resp sendAndWait(
			Req req, long timeout, TimeUnit units) {
		if (null == req) {
			return null;
		}
		String uuid = req.getIdentification();
		ResponseFuture<Resp> responseFuture = new ResponseFuture<Resp>();
		getResponseContext().put(uuid, responseFuture);
		
		addToPending(req);
		try {
			return responseFuture.get(timeout, units);
		} catch (Exception e) {
			LOG.error("", e);
			return null;
		} finally {
			responseFuture = (ResponseFuture<Resp>) getResponseContext()
					.getAndRemove(uuid);
			if (responseFuture != null) {
				responseFuture.cancel(false);
			}
		}
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
