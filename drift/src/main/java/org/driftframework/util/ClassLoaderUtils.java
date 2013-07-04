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
package org.driftframework.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toolkit.lang.PackageUtil;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: ClassLoaderUtils, v 0.1 2013年7月4日 下午7:03:19 Exp $
 */
public abstract class ClassLoaderUtils {
	
	private final static Logger	LOG	= LoggerFactory
											.getLogger(ClassLoaderUtils.class);
	
	private ClassLoaderUtils() {
		//
	}
	
	static public List<Class<?>> loaderClass(String packages) {
		List<Class<?>> list = new ArrayList<Class<?>>();
		try {
			String[] clsNames = PackageUtil.findClassesInPackage(packages,
					null, null);
			for (String clsName : clsNames) {
				try {
					ClassLoader loader = Thread.currentThread()
							.getContextClassLoader();
					if (LOG.isDebugEnabled()) {
						LOG.debug("using ClassLoader [{}] to load Class [{}]",
								new Object[] { loader, clsName });
					}
					Class<?> cls = loader.loadClass(clsName);
					list.add(cls);
					if (LOG.isInfoEnabled()) {
						LOG.info("loader [{}]", new Object[] { cls });
					}
				} catch (ClassNotFoundException e) {
					LOG.error("class loader: [{}]", e);
				}
			}
		} catch (IOException e) {
			LOG.error("class loader: [{}]", e);
		}
		return list;
	}
	
}
