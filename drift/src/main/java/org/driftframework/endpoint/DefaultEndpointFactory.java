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

import org.driftframework.cache.DefaultHolder;
import org.driftframework.cache.Holder;
import org.driftframework.receiver.Receiver;
import org.jboss.netty.channel.Channel;

import com.toolkit.lang.Closure;

/**
 * Create the Endpoint Factory
 * 
 * @author qiaofeng
 * @version $Id: DefaultEndpointFactory, v 0.1 2013年7月3日 上午11:49:13 Exp $
 */
public class DefaultEndpointFactory implements EndpointFactory {
	
	private Closure					nextClosure			= null;
	private Receiver				receiver			= null;
	private Holder<String, Object>	responseContext		= new DefaultHolder<String, Object>();
	private int						cachedMessageCount	= 1024;
	
	@Override
	public Endpoint createEndpoint(Channel channel) {
		Endpoint endpoint = new DefaultEndpoint();
		endpoint.setChannel(channel);
		endpoint.setQueueSize(this.cachedMessageCount);
		endpoint.setNextClosure(this.nextClosure);
		endpoint.setReceiver(this.receiver);
		endpoint.setResponseContext(this.responseContext);
		
		endpoint.start();
		
		return endpoint;
	}
	
	public Closure getNextClosure() {
		return nextClosure;
	}
	
	@Override
	public void setNextClosure(Closure nextClosure) {
		this.nextClosure = nextClosure;
	}
	
	public Receiver getReceiver() {
		return receiver;
	}
	
	@Override
	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}
	
	public Holder<String, Object> getResponseContext() {
		return responseContext;
	}
	
	@Override
	public void setResponseContext(Holder<String, Object> responseContext) {
		this.responseContext = responseContext;
	}
	
	public int getCachedMessageCount() {
		return cachedMessageCount;
	}
	
	@Override
	public void setCachedMessageCount(int cachedMessageCount) {
		this.cachedMessageCount = cachedMessageCount;
	}
	
}
