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

import java.util.Map;

import org.driftframework.context.Context;
import org.driftframework.util.IpPortPair;

/**
 * Drift Server define interface
 * 
 * @author qiaofeng
 * @version $Id: DriftServer, v 0.1 2013-6-30 下午12:16:24 Exp $
 */
public interface Server {
	
	void setHostsAndPorts(String hostsAndPorts);
	
	void setHostAndPort(String host, int port);
	
	void setContext(ServerContext context);
	
	Context getContext();
	
	Map<IpPortPair, Acceptor> getAcceptors();
	
	Acceptor getAcceptor(IpPortPair pair);
	
}
