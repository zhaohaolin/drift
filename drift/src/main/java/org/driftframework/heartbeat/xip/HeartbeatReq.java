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

import org.driftframework.annotation.MessageCode;
import org.driftframework.protocol.AbstractXipRequest;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: HeartbeatReq, v 0.1 2013年7月4日 下午4:40:59 Exp $
 */
@MessageCode(100001)
public class HeartbeatReq extends AbstractXipRequest {
	
	private ServerStatus	serverStatus	= new ServerStatus();
	
	public ServerStatus getServerStatus() {
		return serverStatus;
	}
	
	public void setServerStatus(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;
	}
	
	public ServerCategory getServerCategory() {
		return this.serverStatus.getCategory();
	}
	
	public HeartbeatReq setCategory(String category) {
		getServerCategory().setCategory(category);
		return this;
	}
	
	public String getDomain() {
		return getServerCategory().getDomain();
	}
	
	public String getGroup() {
		return getServerCategory().getGroup();
	}
	
	public void setIp(String ip) {
		this.serverStatus.setIp(ip);
	}
	
	public void setPort(int port) {
		this.serverStatus.setPort(port);
	}
	
	public void setVersion(String version) {
		this.serverStatus.setVersion(version);
	}
	
}
