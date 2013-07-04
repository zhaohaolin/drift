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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: ChooseFirstTestCase, v 0.1 2013年7月2日 下午5:15:16 Exp $
 */
public class ChooseFirstTestCase {
	
	private final static Logger	LOG	= LoggerFactory
											.getLogger(ChooseFirstTestCase.class);
	
	private Scheduling			scheduling;
	
	@Before
	public void before() {
		scheduling = new ChooseFirst();
		scheduling.setTotal(10);
		if (LOG.isInfoEnabled())
			LOG.info("init scheduling finished.");
	}
	
	public static void main(String[] args) {
		if (ChooseFirst.class.isAssignableFrom(Scheduling.class))
			System.out.println("ddddd");
	}
	
	@Test
	public void test() {
		Assert.assertEquals(0, scheduling.next());
		Assert.assertEquals(0, scheduling.index());
		
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					int index = scheduling.next();
					if (LOG.isInfoEnabled()) {
						LOG.info("index=[{}]", index);
					}
					Assert.assertEquals(0, index);
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
