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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.driftframework.annotation.MessageCode;
import org.driftframework.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * default implements for protocol bean define class factory
 * 
 * @author qiaofeng
 * @version $Id: DefaultXipCodecProvider, v 0.1 2013-6-30 下午10:08:59 Exp $
 */
public class DefaultCodecProvider implements XipCodecProvider {
	
	private final static Logger								LOG		= LoggerFactory
																			.getLogger(DefaultCodecProvider.class);
	private final static Kryo								KRYO	= new Kryo();
	private final static ConcurrentMap<Integer, Class<?>>	MAP		= new ConcurrentHashMap<Integer, Class<?>>();
	
	public DefaultCodecProvider() {
		//
	}
	
	static {
		KRYO.setReferences(true);
		KRYO.register(java.util.UUID.class);
	}
	
	@Override
	public void protocolPackages(String... packages) {
		for (String pack : packages) {
			// 分别扫描各包下的协议类
			List<Class<?>> classes = ClassLoaderUtils.loaderClass(pack);
			if (null != classes && !classes.isEmpty()) {
				for (Class<?> clazz : classes) {
					MessageCode code = clazz.getAnnotation(MessageCode.class);
					if (null != code) {
						int value = code.value();
						MAP.put(value, clazz);
					}
					KRYO.register(clazz);
					if (LOG.isInfoEnabled()) {
						LOG.info("Register Protocol Class=[{}]", clazz);
					}
				}
			}
		}
	}
	
	@Override
	public int getMessageId(Class<?> clazz) {
		Iterator<Entry<Integer, Class<?>>> iter = MAP.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Class<?>> entry = iter.next();
			if (clazz.equals(entry.getValue()))
				return entry.getKey();
		}
		return 0;
	}
	
	@Override
	public byte[] encode(Object msg, Class<?> clazz) {
		Output output = new Output(4096, Integer.MAX_VALUE);
		KRYO.writeObject(output, msg);
		byte[] bytes = output.toBytes();
		output.flush();
		output = null;
		return bytes;
	}
	
	@Override
	public Class<?> getMessageClass(int msgId) {
		return MAP.get(msgId);
	}
	
	@Override
	public Object decode(byte[] bytes, int msgId) {
		Input input = new Input(bytes);
		Class<?> clazz = MAP.get(msgId);
		Object obj = KRYO.readObject(input, clazz);
		input.close();
		input = null;
		return obj;
	}
	
}
