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
package org.driftframework.codec;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: XipCodecProvider, v 0.1 2013-6-30 下午9:47:43 Exp $
 */
public interface XipCodecProvider {
	
	// 扫描协议类包路径
	void protocolPackages(String... packages);
	
	// 通过类取得协议Id
	int getMessageId(Class<?> clazz);
	
	// 对对象进行消息编码
	byte[] encode(Object msg, Class<?> clazz);
	
	// 通过消息协议Id取得协议定义类
	Class<?> getMessageClass(int msgId);
	
	// 对消息字节码进行解码
	Object decode(byte[] bytes, int msgId);
	
}
