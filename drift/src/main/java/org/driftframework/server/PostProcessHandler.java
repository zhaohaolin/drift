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

import org.driftframework.context.Context;

/**
 * 后置处理器接口定义,要作事务的后置处理器，必须实现此接口，然后注册到Dispatcher中去
 * 
 * @author qiaofeng
 * @version $Id: PostProcessHandler, v 0.1 2013年7月6日 下午7:19:40 Exp $
 */
public interface PostProcessHandler {
	
	void process(final Context context, final Object msg);
	
}
