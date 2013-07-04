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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: ServerGroup, v 0.1 2013年7月4日 下午4:50:30 Exp $
 */
public class ServerGroup {
	
	private String				serverType;
	
	private List<ServerStatus>	servers	= new ArrayList<ServerStatus>();
	
	public String getServerType() {
		return serverType;
	}
	
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	
	public List<ServerStatus> getServers() {
		return servers;
	}
	
	public void setServers(ArrayList<ServerStatus> servers) {
		this.servers = servers;
	}
	
	public void addServer(ServerStatus server) {
		this.servers.add(server);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
