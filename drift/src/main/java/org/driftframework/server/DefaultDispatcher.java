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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.driftframework.annotation.Controller;
import org.driftframework.annotation.Path;
import org.driftframework.protocol.Xip;
import org.driftframework.receiver.Receiver;
import org.driftframework.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolkit.lang.ClassUtil;

/**
 * 前端控制器
 * 
 * @author qiaofeng
 * @version $Id: DefaultDispatcher, v 0.1 2013-7-1 上午12:04:29 Exp $
 */
public class DefaultDispatcher implements Dispatcher, Receiver {
	
	private final static Logger				LOG				= LoggerFactory
																	.getLogger(DefaultDispatcher.class);
	private final Map<String, Method>		methodTable		= new ConcurrentHashMap<String, Method>();
	private final Map<Method, Class<?>>		clazzTable		= new ConcurrentHashMap<Method, Class<?>>();
	private final Map<Method, Class<?>[]>	paramsTable		= new ConcurrentHashMap<Method, Class<?>[]>();
	private final List<PreProcessHandler>	preProcessList	= new ArrayList<PreProcessHandler>();
	private final List<PostProcessHandler>	postProcessList	= new ArrayList<PostProcessHandler>();
	private ExecutorService					mainExecutor	= Executors
																	.newSingleThreadExecutor();
	private ServerContext					context;
	
	@Override
	public void messageReceived(final Object input) {
		if (input instanceof Xip) {
			final Xip msg = (Xip) input;
			Runnable task = new Runnable() {
				
				@Override
				public void run() {
					
					String url = msg.getPath();
					Method method = methodTable.get(url);
					if (null == method) {
						LOG.error("No course class found for ["
								+ msg.getClass().getName()
								+ "]. Process stopped.");
						return;
					}
					try {
						// 运行前置处理器
						invokePreProcesses(msg);
						
						// 运行业务接口的方法
						invokeBizMethod(method, msg);
						
						// 运行后置处理器
						invokePostProcesses(msg);
					} catch (Exception e) {
						LOG.error("biz error [{}].", e);
					}
				}
				
			};
			
			if (mainExecutor != null) {
				mainExecutor.submit(task);
			} else {
				task.run();
			}
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn("input is not Xip instance obj.");
			}
		}
		
	}
	
	// 运行前置处理器
	private void invokePreProcesses(final Xip msg) {
		if (null != preProcessList && preProcessList.isEmpty()) {
			for (PreProcessHandler handler : preProcessList) {
				handler.process(context, msg);
			}
		}
	}
	
	// 运行后置处理器
	private void invokePostProcesses(final Xip msg) {
		if (null != postProcessList && postProcessList.isEmpty()) {
			for (PostProcessHandler handler : postProcessList) {
				handler.process(context, msg);
			}
		}
	}
	
	private void invokeBizMethod(Method method, final Xip msg) {
		if (null != method) {
			try {
				Class<?> course = clazzTable.get(method);
				// arg0: context, arg1: req, arg2: resp
				method.invoke(course, context, msg);
			} catch (Exception e) {
				LOG.error("Invoke biz method [" + method.getName()
						+ "] failed. " + e);
			}
		} else {
			LOG.error("No biz method found for message ["
					+ msg.getClass().getName() + "]. No process execute.");
		}
	}
	
	public void setControllers(String... packs) {
		for (String pack : packs) {
			setControllers(pack);
		}
	}
	
	public void setControllers(String packages) {
		List<Class<?>> list = ClassLoaderUtils.loaderClass(packages);
		if (null != list && !list.isEmpty()) {
			setCourses(list);
			if (LOG.isDebugEnabled()) {
				LOG.debug("get classes=[{}] for packages=[{}]", new Object[] {
						list, packages });
			}
		}
	}
	
	public <T> void setCourses(Collection<T> courses) {
		for (T t : courses) {
			
			// 如果没有实现注解Controller，说明不是业务实现控制器
			if (!t.getClass().isAnnotationPresent(Controller.class)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("[{}] is not drift controller implements class.",
							t);
				}
				continue;
			}
			
			// 解析处理方法及参数
			Method[] methods = ClassUtil.getAllMethodOf(t.getClass());
			for (Method method : methods) {
				Path path = method.getAnnotation(Path.class);
				if (null != path) {
					String url = path.value();
					Class<?>[] params = method.getParameterTypes();
					if (params.length < 1) {
						continue;
					}
					
					// 把方法保存进Hash列表中
					methodTable.put(url, method);
					// 把参数保存进Hash列表中
					paramsTable.put(method, params);
					
					clazzTable.put(method, t.getClass());
				}
			}
		}
	}
	
	public void setThreads(int threads) {
		this.mainExecutor = Executors.newFixedThreadPool(threads);
	}
	
	@Override
	public void setContext(ServerContext context) {
		this.context = context;
	}
	
	@Override
	public void setPreProcess(PreProcessHandler handler) {
		this.preProcessList.add(handler);
	}
	
	@Override
	public void setPostProcess(PostProcessHandler handler) {
		this.postProcessList.add(handler);
	}
	
	@Override
	public void setPreProcesses(List<PreProcessHandler> list) {
		if (null != list && !list.isEmpty()) {
			for (PreProcessHandler handler : list) {
				this.preProcessList.add(handler);
			}
		}
	}
	
	@Override
	public void setPostProcesses(List<PostProcessHandler> list) {
		if (null != list && !list.isEmpty()) {
			for (PostProcessHandler handler : list) {
				this.postProcessList.add(handler);
			}
		}
	}
	
}
