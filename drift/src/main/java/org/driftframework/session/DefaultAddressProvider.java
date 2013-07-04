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
package org.driftframework.session;

import java.net.InetSocketAddress;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultAddressProvider, v 0.1 2013-6-30 下午7:54:47 Exp $
 */
public class DefaultAddressProvider implements AddressProvider {
	
	private String	destIp		= "127.0.0.1";
	private int		destPort	= 8080;
	
	public DefaultAddressProvider(String ip, int port) {
		this.destIp = ip;
		this.destPort = port;
	}
	
	/**
	 * @param destIp
	 *            the destIp to set
	 */
	public void setDestIp(String destIp) {
		this.destIp = destIp;
	}
	
	/**
	 * @param destPort
	 *            the destPort to set
	 */
	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	
	@Override
	public InetSocketAddress getAddress() {
		return new InetSocketAddress(destIp, destPort);
	}
	
}
