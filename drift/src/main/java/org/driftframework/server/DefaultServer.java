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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.driftframework.util.IpPortPair;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultDriftServer, v 0.1 2013-6-30 下午9:42:59 Exp $
 */
public class DefaultServer implements Server {
	
	private final static List<IpPortPair>			IPPORTPAIRS	= new ArrayList<IpPortPair>();
	private final static Map<IpPortPair, Acceptor>	ACCEPTORS	= new ConcurrentHashMap<IpPortPair, Acceptor>();
	private ServerContext							context		= new DefaultServerContext();
	
	public void start() {
		if (null != IPPORTPAIRS && !IPPORTPAIRS.isEmpty()) {
			Acceptor acceptor = null;
			for (IpPortPair pair : IPPORTPAIRS) {
				acceptor = new DefaultAcceptor();
				acceptor.setContext(context);
				acceptor.setExportIp(pair.getIp());
				acceptor.setExportPort(pair.getPort());
				acceptor.start();
				ACCEPTORS.put(pair, acceptor);
			}
		}
	}
	
	public void stop() {
		if (null != IPPORTPAIRS && !IPPORTPAIRS.isEmpty()) {
			Acceptor acceptor = null;
			for (IpPortPair pair : IPPORTPAIRS) {
				acceptor = ACCEPTORS.get(pair);
				acceptor.stop();
			}
		}
	}
	
	@Override
	public void setHostsAndPorts(String hostsAndPorts) {
		String[] arr = hostsAndPorts.split("\\/");
		if (null != arr) {
			for (String s : arr) {
				String[] a = s.split("\\:");
				if (null != a) {
					IpPortPair pair = null;
					pair = new IpPortPair(a[0], Integer.valueOf(a[1]));
					IPPORTPAIRS.add(pair);
				}
			}
		}
		
	}
	
	@Override
	public void setHostAndPort(String host, int port) {
		IpPortPair pair = new IpPortPair(host, port);
		IPPORTPAIRS.add(pair);
	}
	
	/**
	 * @return the context
	 */
	public ServerContext getContext() {
		return context;
	}
	
	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(ServerContext context) {
		this.context = context;
	}
	
	public Map<IpPortPair, Acceptor> getAcceptors() {
		return ACCEPTORS;
	}
	
	public Acceptor getAcceptor(IpPortPair pair) {
		return ACCEPTORS.get(pair);
	}
	
}
