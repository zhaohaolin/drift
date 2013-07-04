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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultEndpointRepository, v 0.1 2013年7月3日 上午11:56:05 Exp $
 */
public class DefaultEndpointRepository implements EndpointRepository {
	
	private Logger			log				= LoggerFactory
													.getLogger(DefaultEndpointRepository.class);
	private List<Endpoint>	sessionStore	= new CopyOnWriteArrayList<Endpoint>();
	private AtomicInteger	sessionIdx		= new AtomicInteger(0);
	private int				maxSessions		= 1;
	
	@Override
	public void addEndpoint(Endpoint endpoint) {
		sessionStore.add(endpoint);
	}
	
	@Override
	public void removeEndpoint(Endpoint endpoint) {
		sessionStore.remove(endpoint);
	}
	
	@Override
	public List<Endpoint> getEndpoints() {
		return sessionStore;
	}
	
	@Override
	public Endpoint getEndpoint() {
		List<Endpoint> endpoints = getEndpoints();
		while (0 == endpoints.size()) {
			try {
				Thread.sleep(1 * 1000);
			} catch (InterruptedException e) {
				log.error("send :", e);
			}
			endpoints = getEndpoints();
		}
		
		if (endpoints.size() == 1) {
			return endpoints.get(0);
		}
		
		// decide which idx to get session
		int idx = sessionIdx.getAndIncrement();
		if (idx >= endpoints.size()) {
			idx = 0;
			sessionIdx.set(idx);
		}
		
		return endpoints.get(idx);
	}
	
	@Override
	public void setMaxSession(int maxSessions) {
		this.maxSessions = maxSessions;
	}
	
	@Override
	public boolean isFull() {
		return sessionStore.size() >= maxSessions;
	}
	
	@Override
	public int getMaxSession() {
		return this.maxSessions;
	}
	
}
