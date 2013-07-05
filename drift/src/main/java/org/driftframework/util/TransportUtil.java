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
package org.driftframework.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.driftframework.client.Sender;
import org.driftframework.endpoint.Endpoint;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelLocal;

import com.toolkit.lang.Propertyable;

/**
 * the toolkit TransportUtil for app used
 * 
 * @author qiaofeng
 * @version $Id: TransportUtil, v 0.1 2013年7月2日 下午11:06:50 Exp $
 */
public abstract class TransportUtil {
	
	private static final String											TRANSPORT_ENDPOINT	= "TRANSPORT_ENDPOINT";
	private static final String											TRANSPORT_SENDER	= "TRANSPORT_SENDER";
	private static final ChannelLocal<ConcurrentMap<String, Object>>	LOCAL				= new ChannelLocal<ConcurrentMap<String, Object>>();
	
	public final static void setAttribute(Channel channel, String key,
			Object value) {
		ConcurrentMap<String, Object> attributeMap = LOCAL.get(channel);
		if (null == attributeMap) {
			attributeMap = new ConcurrentHashMap<String, Object>();
			LOCAL.set(channel, attributeMap);
		}
		attributeMap.put(key, value);
	}
	
	public final static Object getAttribute(Channel channel, String key) {
		ConcurrentMap<String, Object> attributeMap = LOCAL.get(channel);
		if (null == attributeMap) {
			attributeMap = new ConcurrentHashMap<String, Object>();
			LOCAL.set(channel, attributeMap);
		}
		return attributeMap.get(key);
	}
	
	public final static boolean removeAttribute(Channel channel, String key) {
		boolean bool = false;
		ConcurrentMap<String, Object> attributeMap = LOCAL.get(channel);
		if (null == attributeMap) {
			bool = false;
		} else {
			Object obj = attributeMap.remove(key);
			if (null != obj)
				bool = true;
		}
		return bool;
	}
	
	public final static void addEndpointToChannel(Channel channel,
			Endpoint endpoint) {
		setAttribute(channel, TRANSPORT_ENDPOINT, endpoint);
	}
	
	public final static Endpoint getEndpointOfChannel(Channel channel) {
		return (Endpoint) getAttribute(channel, TRANSPORT_ENDPOINT);
	}
	
	public final static Object attachSender(Object propertyable, Sender sender) {
		if (propertyable instanceof Propertyable) {
			((Propertyable) propertyable).setProperty(TRANSPORT_SENDER, sender);
		}
		return propertyable;
	}
	
	public final static Sender getSenderOf(Object propertyable) {
		if (propertyable instanceof Propertyable) {
			return (Sender) ((Propertyable) propertyable)
					.getProperty(TRANSPORT_SENDER);
		}
		return null;
	}
	
}
