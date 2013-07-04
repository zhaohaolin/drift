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
package org.driftframework.endpoint;


/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: EndpointChangeListener, v 0.1 2013年7月2日 下午10:54:52 Exp $
 */
public interface EndpointChangeListener {
	
	void addEndpoint(Endpoint endpoint);
	
	void removeEndpoint(Endpoint endpoint);
	
}
