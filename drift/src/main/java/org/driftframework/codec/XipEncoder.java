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

import org.driftframework.protocol.Xip;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: XipEncoder, v 0.1 2013-6-30 下午10:04:01 Exp $
 */
public class XipEncoder extends OneToOneEncoder {
	
	private final static Logger	LOG				= LoggerFactory
														.getLogger(XipEncoder.class);
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;
	private XipCodecProvider	provider;
	
	public XipEncoder(XipCodecProvider provider) {
		this.provider = provider;
	}
	
	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		byte[] bytes = null;
		if (msg instanceof Xip) {
			
			// 先写通讯握手的Id
			Class<?> clazz = msg.getClass();
			int id = provider.getMessageId(clazz);
			buffer.writeInt(id);
			
			// 再对数据进行编码
			bytes = provider.encode(msg, clazz);
			
			if (null == bytes) {
				LOG.error("encode: [{}] can not generate byte stream.", msg);
				throw new RuntimeException("encode: bean " + msg
						+ " is not XipProtocol.");
			}
			
			// 写消息长度
			buffer.writeInt(bytes.length);
			
			// 写消息体
			buffer.writeBytes(bytes);
			
			if (LOG.isTraceEnabled()) {
				LOG.trace("bean type [{}] and encode size is [{}]",
						new Object[] { msg.getClass(), bytes.length });
			}
			
		} else if (byte[].class.isAssignableFrom(msg.getClass())) {
			bytes = (byte[]) msg;
		} else {
			LOG.error("encode: [{}] can not generate byte stream.", msg);
			throw new RuntimeException("encode: bean " + msg + " is not Xip.");
		}
		
		return buffer;
	}
	
	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}
	
	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}
	
	public int getDumpBytes() {
		return dumpBytes;
	}
	
	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}
	
	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(XipCodecProvider provider) {
		this.provider = provider;
	}
}
