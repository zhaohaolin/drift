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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.driftframework.cache.DefaultHolder;
import org.driftframework.cache.Holder;
import org.driftframework.protocol.Xip;
import org.driftframework.protocol.XipRequest;
import org.driftframework.protocol.XipResponse;
import org.driftframework.receiver.Receiver;
import org.driftframework.response.ResponseClosure;
import org.driftframework.routing.ChooseFirst;
import org.driftframework.routing.RoundRobin;
import org.driftframework.routing.Scheduling;
import org.driftframework.routing.SchedulingStrategy;
import org.driftframework.util.IpPortPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolkit.lang.Closure;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultRouter, v 0.1 2013年7月4日 下午5:01:43 Exp $
 */
public class DefaultRouter implements Router {
	
	private final static Logger						LOG					= LoggerFactory
																				.getLogger(DefaultRouter.class);
	private ConcurrentMap<IpPortPair, Connector>	connectors			= new ConcurrentHashMap<IpPortPair, Connector>();
	private Closure									nextClosure			= null;
	private Receiver								receiver			= null;
	private int										cachedMessageCount	= 1024;
	private int										maxSession			= 1;
	private Holder<String, Object>					responseContext		= new DefaultHolder<String, Object>();
	private long									reconnectTimeout	= 1;
	private List<IpPortPair>						snapshot			= new ArrayList<IpPortPair>();
	private AtomicReference<IpPortPair[]>			routesRef			= new AtomicReference<IpPortPair[]>(
																				new IpPortPair[0]);
	private Scheduling								scheduling			= new RoundRobin();
	private String									name;
	
	private ClientContext							context;
	
	private Connector next() {
		int index = scheduling.next();
		if (index >= 0) {
			IpPortPair pair = routesRef.get()[index];
			if (connectors.containsKey(pair)) {
				return connectors.get(pair);
			}
			return createConnector(pair.getIp(), pair.getPort());
		}
		return null;
	}
	
	/**
	 * 创建连接
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private Connector createConnector(String ip, int port) {
		IpPortPair pair = new IpPortPair(ip, port);
		Connector connector = connectors.get(pair);
		
		if (null == connector) {
			connector = new DefaultConnector();
			Connector oldConnector = connectors.putIfAbsent(pair, connector);
			if (null != oldConnector) {
				connector.stop();
				connector = oldConnector;
			} else {
				
				connector.setCachedMessageCount(this.cachedMessageCount);
				connector.setNextClosure(this.nextClosure);
				connector.setReceiver(this.receiver);
				connector.setResponseContext(this.responseContext);
				connector.setMaxSession(this.maxSession);
				
				// connector.setCodecFactory(this.codecFactory);
				connector.setDestIp(ip);
				connector.setDestPort(port);
				connector.setReconnectTimeout(this.reconnectTimeout);
				
				connector.start();
			}
		}
		return connector;
	}
	
	/**
	 * 刷新路由
	 * 
	 * @param infos
	 */
	public void doRefreshRoute(List<IpPortPair> infos) {
		
		Collections.sort(infos);
		
		if (!snapshot.equals(infos)) {
			LOG.info(
					"doRefreshRoute [{}]: update routes info:[{}]/lastRoutes:[{}].",
					new Object[] { name, infos, snapshot });
			
			snapshot.clear();
			snapshot.addAll(infos);
			routesRef.set(snapshot.toArray(new IpPortPair[0]));
			scheduling.setTotal(routesRef.get().length);
		}
		
		// 删除无效连接
		for (IpPortPair key : connectors.keySet()) {
			if (!snapshot.contains(key)) {
				Connector out = connectors.get(key);
				if (null != out) {
					out.stop();
				}
				connectors.remove(key);
			}
		}
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}
	
	// 以IP和port直接配置
	public void setHosts(String hosts) {
		try {
			String[] hostArray;
			if (hosts.indexOf("/") == -1) {
				hostArray = new String[] { hosts };
			} else {
				hostArray = hosts.split("/");
			}
			
			List<IpPortPair> infos = new ArrayList<IpPortPair>();
			for (int i = 0; i < hostArray.length; i++) {
				String ipPort = hostArray[i];
				if (ipPort == null)
					break;
				String[] server = ipPort.split(":");
				if (server.length == 2) {
					IpPortPair ipPortPair = new IpPortPair(server[0].trim(),
							Integer.parseInt(server[1].trim()));
					infos.add(ipPortPair);
				} else {
					throw new RuntimeException("host [" + ipPort
							+ "] not match IP:PORT");
				}
			}
			this.doRefreshRoute(infos);
		} catch (Exception ex) {
			LOG.error(">>>> config occurs error. (hosts ParseException)", ex);
			System.exit(0);
		}
	}
	
	public void setNextClosure(Closure nextClosure) {
		this.nextClosure = nextClosure;
	}
	
	public void setReceiver(Receiver receiver) {
		this.receiver = receiver;
	}
	
	public void setCachedMessageCount(int cachedMessageCount) {
		this.cachedMessageCount = cachedMessageCount;
	}
	
	public void setMaxSession(int maxSession) {
		this.maxSession = maxSession;
	}
	
	public void setResponseContext(Holder<String, Object> responseContext) {
		this.responseContext = responseContext;
	}
	
	public void setReconnectTimeout(long reconnectTimeout) {
		this.reconnectTimeout = reconnectTimeout;
	}
	
	/**
	 * @return the context
	 */
	public ClientContext getContext() {
		return context;
	}
	
	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(ClientContext context) {
		this.context = context;
	}
	
	public ConcurrentMap<IpPortPair, Connector> getConnectors() {
		return connectors;
	}
	
	public List<IpPortPair> getSnapshot() {
		return snapshot;
	}
	
	public AtomicReference<IpPortPair[]> getRoutesRef() {
		return routesRef;
	}
	
	public Scheduling getScheduling() {
		return scheduling;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRoutingStrategy(SchedulingStrategy strategy) {
		if (SchedulingStrategy.CHOOSE_FIRST == strategy) {
			this.scheduling = new ChooseFirst();
		} else if (SchedulingStrategy.ROUND_ROBIN == strategy) {
			this.scheduling = new RoundRobin();
		} else {
			throw new UnsupportedOperationException("SchedulingStrategy ["
					+ strategy + "] not implemend yet.");
		}
	}
	
	@Override
	public <Req extends Xip> void send(Req req) {
		Connector connector = next();
		if (connector != null) {
			connector.send(req);
			if (LOG.isTraceEnabled()) {
				LOG.trace("send: connector=[{}], bean=[{}]", new Object[] {
						connector, req });
			}
		} else {
			if (LOG.isErrorEnabled()) {
				LOG.error("send: no route, msg [{}] lost. route=[{}]",
						new Object[] { req, name });
			}
		}
	}
	
	@Override
	public <Req extends XipRequest, Resp extends XipResponse> void send(
			Req req, ResponseClosure<Resp> callback) {
		Connector connector = next();
		if (connector != null) {
			connector.send(req, callback);
			if (LOG.isTraceEnabled()) {
				LOG.trace("send: connector=[{}], bean=[{}]", new Object[] {
						connector, req });
			}
		} else {
			if (LOG.isErrorEnabled()) {
				LOG.error("send: no route, msg [{}] lost. route=[{}]",
						new Object[] { req, name });
			}
		}
	}
	
	@Override
	public <Req extends XipRequest, Resp extends XipResponse> Resp sendAndWait(
			Req req) {
		Connector connector = next();
		if (connector != null) {
			LOG.trace("sendAndWait: connector=[{}], bean=[{}]", new Object[] {
					connector, req });
			return connector.sendAndWait(req);
		}
		
		if (LOG.isErrorEnabled())
			LOG.error("send: no route, msg [{}] lost. route=[{}]",
					new Object[] { req, name });
		return null;
	}
	
	@Override
	public <Req extends Xip, Resp extends XipResponse> Resp sendAndWait(
			Req req, long timeout, TimeUnit units) {
		Connector connector = next();
		if (connector != null) {
			LOG.trace(
					"sendAndWait: connector=[{}], bean=[{}], timeout=[{}], timeUnit=[{}]",
					new Object[] { connector, req, timeout, units });
			return connector.sendAndWait(req, timeout, units);
		}
		
		if (LOG.isErrorEnabled())
			LOG.error("send: no route, msg [{}] lost. route=[{}]",
					new Object[] { req, name });
		return null;
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
