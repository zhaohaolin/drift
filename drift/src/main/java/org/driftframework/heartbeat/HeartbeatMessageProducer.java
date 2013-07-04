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

import java.util.concurrent.Callable;

import org.driftframework.heartbeat.xip.HeartbeatReq;

import com.toolkit.lang.AppInfo;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: HeartbeatMessageProducer, v 0.1 2013年7月4日 下午4:40:25 Exp $
 */
public class HeartbeatMessageProducer implements Callable<HeartbeatReq> {
	
	private String	ip;
	private int		port;
	private String	category;
	
	private AppInfo	appInfo;
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public void setAppInfo(AppInfo appInfo) {
		this.appInfo = appInfo;
	}
	
	protected HeartbeatReq setCommonAttr(HeartbeatReq req) {
		req.setCategory(category);
		req.setIp(ip);
		req.setPort(port);
		
		// 设置版本号
		if (appInfo != null) {
			req.setVersion(appInfo.getAppVersion());
		}
		
		return req;
	}
	
	@Override
	public HeartbeatReq call() {
		HeartbeatReq req = new HeartbeatReq();
		this.setCommonAttr(req);
		return req;
	}
	
}
