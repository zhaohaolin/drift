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
package org.driftframework.context;

import java.util.List;

import org.driftframework.codec.DefaultCodecProvider;
import org.driftframework.codec.XipCodecProvider;
import org.driftframework.protocol.Xip;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: DefaultDriftContext, v 0.1 2013-6-30 下午8:34:04 Exp $
 */
public abstract class DefaultContext implements Context {
	
	private XipCodecProvider	codecProvider	= new DefaultCodecProvider();
	
	public DefaultContext() {
		//
	}
	
	@Override
	public void setProtocols(String packages) {
		String[] packs = packages.split("\\,");
		codecProvider.protocolPackages(packs);
	}
	
	@Override
	public void addProtocols(List<Xip> lists) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public XipCodecProvider getXipCodecProvider() {
		return codecProvider;
	}
	
	public void setXipCodecProvider(XipCodecProvider codecProvider) {
		this.codecProvider = codecProvider;
	}
	
}
