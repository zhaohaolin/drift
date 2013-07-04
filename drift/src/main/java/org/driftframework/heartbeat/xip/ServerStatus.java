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
package org.driftframework.heartbeat.xip;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: ServerStatus, v 0.1 2013年7月4日 下午4:45:35 Exp $
 */
public class ServerStatus {
	
	/** 通信服务器IP */
	private String			ip;
	
	/** 通信服务器端口 */
	private int				port;
	
	private String			version;
	
	private ServerCategory	category	= new ServerCategory();
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public ServerCategory getCategory() {
		return category;
	}
	
	public void setCategory(ServerCategory category) {
		this.category = category;
	}
	
	public void setDomain(String domain) {
		this.category.setDomain(domain);
	}
	
	public void setGroup(String group) {
		this.category.setGroup(group);
	}
	
	public boolean isSameDomain(ServerStatus server) {
		return this.category.isSameDomain(server.getCategory());
	}
	
	public String getDomain() {
		return this.category.getDomain();
	}
	
	public String getGroup() {
		return this.category.getGroup();
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
