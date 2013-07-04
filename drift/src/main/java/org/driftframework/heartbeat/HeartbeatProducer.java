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
package org.driftframework.heartbeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.driftframework.client.Connector;
import org.driftframework.client.Router;
import org.driftframework.heartbeat.xip.HeartbeatReq;
import org.driftframework.heartbeat.xip.HeartbeatResp;
import org.driftframework.heartbeat.xip.ServerGroup;
import org.driftframework.heartbeat.xip.ServerStatus;
import org.driftframework.response.ResponseClosure;
import org.driftframework.util.IpPortPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: HeartbeatProducer, v 0.1 2013年7月4日 下午4:40:16 Exp $
 */
public class HeartbeatProducer {
	
	private final static Logger			LOG					= LoggerFactory
																	.getLogger(HeartbeatProducer.class);
	private HeartbeatMessageProducer	messageProducer;
	private Connector					connector;
	private ScheduledExecutorService	scheduler			= Executors
																	.newSingleThreadScheduledExecutor();
	private long						heartbeatInterval	= 5 * 1000;
	private Map<String, Router>			routers				= new ConcurrentHashMap<String, Router>();
	
	private List<IpPortPair> convert(ServerGroup group) {
		List<ServerStatus> servers = group.getServers();
		List<IpPortPair> ret = new ArrayList<IpPortPair>(null == servers ? 0
				: servers.size());
		if (null != servers) {
			for (ServerStatus info : servers) {
				ret.add(new IpPortPair(info.getIp(), info.getPort()));
			}
		}
		return ret;
	}
	
	public void start() {
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				// 生成心跳请求
				final HeartbeatReq req = messageProducer.call();
				if (LOG.isTraceEnabled()) {
					LOG.trace("send connector=[{}], HeartbeatReq=[{}]",
							connector, req);
				}
				
				// 向中央控制器发送心跳包消息
				connector.send(req, new ResponseClosure<HeartbeatResp>() {
					
					@Override
					public void onResponse(final HeartbeatResp resp) {
						
						// 返回心跳包响应
						if (LOG.isTraceEnabled()) {
							LOG.trace(
									"response connector=[{}], HeartbeatResp=[{}]",
									new Object[] { connector, resp });
						}
						
						// 当路由配置不为空时
						if (!routers.isEmpty()) {
							List<ServerGroup> groups = resp.getCandidates();
							for (ServerGroup group : groups) {
								Router router = routers.get(group
										.getServerType());
								if (null != router) {
									if (LOG.isTraceEnabled()) {
										LOG.trace(
												"refresh router router=[{}], group=[{}]",
												new Object[] { router, group });
									}
									router.doRefreshRoute(convert(group));
								}
							}
						}
					}
					
				});
				
			}
		}, 0, heartbeatInterval, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		scheduler.shutdown();
	}
	
	public void setMessageProducer(HeartbeatMessageProducer messageProducer) {
		this.messageProducer = messageProducer;
	}
	
	public HeartbeatMessageProducer getMessageProducer() {
		return messageProducer;
	}
	
	public void setHeartbeatInterval(long heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval * 1000;
	}
	
	public void setConnector(Connector connector) {
		this.connector = connector;
	}
	
	public void setRouters(Map<String, Router> routers) {
		this.routers = routers;
	}
	
}
