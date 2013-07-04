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
package org.driftframework.server;

import java.util.List;

import org.driftframework.context.DefaultContext;
import org.driftframework.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultServerContext, v 0.1 2013年7月4日 下午5:54:07 Exp $
 */
public class DefaultServerContext extends DefaultContext implements
		ServerContext {
	
	private final static Logger	LOG			= LoggerFactory
													.getLogger(DefaultServerContext.class);
	private Dispatcher			dispatcher	= new DefaultDispatcher();
	
	public void start() {
		//
	}
	
	public void stop() {
		//
	}
	
	@Override
	public void setControllers(String packages) {
		List<Class<?>> list = ClassLoaderUtils.loaderClass(packages);
		if (null != list && !list.isEmpty()) {
			addControllers(list);
			if (LOG.isDebugEnabled()) {
				LOG.debug("get classes=[{}] for packages=[{}]", new Object[] {
						list, packages });
			}
		}
	}
	
	@Override
	public void setProtocols(String packages) {
		super.setProtocols(packages);
	}
	
	@Override
	public void addControllers(List<?> lists) {
		dispatcher.setCourses(lists);
	}
	
	@Override
	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
}
