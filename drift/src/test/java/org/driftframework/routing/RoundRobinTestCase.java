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
package org.driftframework.routing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: RoundRobinTestCase, v 0.1 2013年7月2日 下午5:35:57 Exp $
 */
public class RoundRobinTestCase {
	
	private final static Logger	LOG	= LoggerFactory
											.getLogger(RoundRobinTestCase.class);
	
	private Scheduling			scheduling;
	
	@Before
	public void before() {
		scheduling = new RoundRobin();
		scheduling.setTotal(5);
		if (LOG.isInfoEnabled())
			LOG.info("init scheduling finished.");
	}
	
	@Test
	public void test() {
		
		for (int i = 0; i < 20; i++) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					int index = scheduling.next();
					if (LOG.isInfoEnabled()) {
						LOG.info("index=[{}]", index);
					}
				}
				
			}).start();
		}
		
		for (int i = 0; i < 5; i++) {
			final int j = i;
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					scheduling.setTotal(j + 10);
				}
				
			}).start();
		}
	}
	
	@After
	public void after() {
		// scheduling = null;
		if (LOG.isInfoEnabled())
			LOG.info("test scheduling finished.");
	}
	
}
