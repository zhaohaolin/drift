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
package org.driftframework.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * default implements for cached Holder class
 * 
 * @author qiaofeng
 * @version $Id: DefaultHolder, v 0.1 2013-6-30 下午4:02:19 Exp $
 */
public final class DefaultHolder<K, V> implements Holder<K, V> {
	
	// Use this cache MAp for All application in memory.
	private final ConcurrentMap<K, V>	map	= new ConcurrentHashMap<K, V>();
	
	@Override
	public void put(K key, V value) {
		map.put(key, value);
	}
	
	@Override
	public V get(K key) {
		return map.get(key);
	}
	
	@Override
	public V getAndRemove(K key) {
		V ret = get(key);
		map.remove(key);
		return ret;
	}
	
	@Override
	public void remove(K key) {
		map.remove(key);
	}
	
}
