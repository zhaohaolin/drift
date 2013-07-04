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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * ip and port define obj class
 * 
 * @author qiaofeng
 * @version $Id: IpPortPair, v 0.1 2013-6-30 下午2:33:34 Exp $
 */
public class IpPortPair implements Comparable<IpPortPair> {
	
	private String	ip		= "127.0.0.1";
	private int		port	= 0;
	
	public IpPortPair() {
		//
	}
	
	public IpPortPair(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	/**
	 * @return the ip
	 */
	public String getIp() {
		return this.ip;
	}
	
	/**
	 * @param ip
	 *            the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	/**
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public int compareTo(IpPortPair o) {
		int rslt = this.ip.compareTo(o.ip);
		if (0 == rslt) {
			return this.port - o.port;
		}
		return rslt;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((null == ip) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (null == obj)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IpPortPair other = (IpPortPair) obj;
		if (null == ip) {
			if (null != other.ip)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
