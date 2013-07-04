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

import java.util.List;

import org.driftframework.cache.Holder;
import org.driftframework.context.Context;
import org.driftframework.endpoint.EndpointFactory;
import org.driftframework.endpoint.EndpointRepository;
import org.driftframework.receiver.Receiver;
import org.driftframework.session.AddressProvider;
import org.jboss.netty.logging.InternalLoggerFactory;

import com.toolkit.lang.Closure;

/**
 * the client connection define interface class
 * 
 * @author qiaofeng
 * @version $Id: TCPConnector, v 0.1 2013-6-30 下午7:49:21 Exp $
 */
public interface Connector extends Sender {
	
	void start();
	
	void stop();
	
	String getDestIp();
	
	void setDestIp(String destIp);
	
	int getDestPort();
	
	void setDestPort(int destPort);
	
	void setOptions(List<String> options);
	
	void setContext(Context context);
	
	void setLoggerFactory(InternalLoggerFactory loggerFactory);
	
	void setReconnectTimeout(long reconnectTimeout);
	
	void setAddressProvider(AddressProvider addressProvider);
	
	void setEndpointRepository(EndpointRepository endpointRepository);
	
	void setEndpointFactory(EndpointFactory endpointFactory);
	
	void setNextClosure(Closure nextClosure);
	
	void setReceiver(Receiver receiver);
	
	void setCachedMessageCount(int cachedMessageCount);
	
	void setMaxSession(int maxSession);
	
	void setResponseContext(Holder<String, Object> responseContext);
	
}
