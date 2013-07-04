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

/**
 * ChooseFirst for routing role
 * 
 * @author qiaofeng
 * @version $Id: ChooseFirst, v 0.1 2013年7月2日 下午5:14:20 Exp $
 */
public class ChooseFirst implements Scheduling {
	
	private int	total;
	
	@Override
	public void setTotal(int total) {
		this.total = total;
	}
	
	@Override
	public int next() {
		if (total > 0) {
			return 0;
		}
		return -1;
	}
	
	@Override
	public int getTotal() {
		return total;
	}
	
	@Override
	public int index() {
		if (total > 0) {
			return 0;
		}
		return -1;
	}
	
}
