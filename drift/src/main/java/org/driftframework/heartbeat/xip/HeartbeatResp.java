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

import org.driftframework.annotation.MessageCode;
import org.driftframework.protocol.AbstractXipResponse;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: HeartbeatResp, v 0.1 2013年7月4日 下午4:49:47 Exp $
 */
@MessageCode(200001)
public class HeartbeatResp extends AbstractXipResponse {
	
	private List<ServerGroup>	candidates	= new ArrayList<ServerGroup>();
	
	public List<ServerGroup> getCandidates() {
		return candidates;
	}
	
	public void setCandidates(ArrayList<ServerGroup> candidates) {
		this.candidates = candidates;
	}
	
	public void addServerGroup(ServerGroup group) {
		candidates.add(group);
	}
	
}
