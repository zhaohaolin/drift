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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: CacheHolder, v 0.1 2013-6-30 下午3:50:45 Exp $
 */
public class CacheHolder<K, V> implements Holder<K, V> {
	
	private final static Logger	LOG	= LoggerFactory
											.getLogger(CacheHolder.class);
	private Cache				cache;
	
	@Override
	public void put(K key, V value) {
		cache.put(new Element(key, value));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(K key) {
		Element element = cache.get(key);
		if (null != element) {
			return (V) element.getObjectValue();
		}
		return null;
	}
	
	@Override
	public V getAndRemove(K key) {
		V ret = get(key);
		remove(key);
		return ret;
	}
	
	@Override
	public void remove(K key) {
		cache.remove(key);
	}
	
	public void setCache(Cache newCache) {
		this.cache = newCache;
		
		this.cache.getCacheEventNotificationService().registerListener(
				new CacheEventListener() {
					
					@Override
					public void notifyElementRemoved(Ehcache cache,
							Element element) throws CacheException {
						if (LOG.isTraceEnabled()) {
							LOG.trace("notifyElementRemoved:[{}]",
									element.getObjectValue());
						}
					}
					
					@Override
					public void notifyElementPut(Ehcache cache, Element element)
							throws CacheException {
						if (LOG.isTraceEnabled()) {
							LOG.trace("notifyElementPut:[{}]",
									element.getObjectValue());
						}
					}
					
					@Override
					public void notifyElementUpdated(Ehcache cache,
							Element element) throws CacheException {
						if (LOG.isTraceEnabled()) {
							LOG.trace("notifyElementUpdated:[{}]",
									element.getObjectValue());
						}
					}
					
					@Override
					public void notifyElementExpired(Ehcache cache,
							Element element) {
						if (LOG.isTraceEnabled()) {
							LOG.trace("notifyElementExpired:[{}]",
									element.getObjectValue());
						}
					}
					
					@Override
					public void notifyElementEvicted(Ehcache cache,
							Element element) {
						if (LOG.isTraceEnabled()) {
							LOG.trace("notifyElementEvicted:[{}]",
									element.getObjectValue());
						}
					}
					
					@Override
					public void notifyRemoveAll(Ehcache cache) {
						if (LOG.isTraceEnabled()) {
							LOG.trace("notifyRemoveAll.");
						}
					}
					
					@Override
					public void dispose() {
						//
					}
					
					@Override
					public Object clone() throws CloneNotSupportedException {
						throw new CloneNotSupportedException();
					}
					
				});
	}
	
}
