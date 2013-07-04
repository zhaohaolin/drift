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

import org.driftframework.context.Context;
import org.jboss.netty.logging.InternalLoggerFactory;

/**
 * the server acceptor define interface
 * 
 * @author qiaofeng
 * @version $Id: TCPAcceptor, v 0.1 2013-6-30 下午7:49:53 Exp $
 */
public interface Acceptor {
	
	void start();
	
	void stop();
	
	void setContext(Context ctx);
	
	Context getContext();
	
	void setExportIp(String ip);
	
	void setExportPort(int port);
	
	void setLoggerFactory(InternalLoggerFactory loggerFactory);
	
	void setOptions(List<String> options);
	
	void setMaxRetryCount(int maxRetryCount);
	
	void setRetryTimeout(long retryTimeout);
	
}
