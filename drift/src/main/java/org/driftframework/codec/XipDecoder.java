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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: XipDecoder, v 0.1 2013-6-30 下午9:48:01 Exp $
 */
public class XipDecoder extends FrameDecoder {
	
	private final static Logger	LOG					= LoggerFactory
															.getLogger(XipDecoder.class);
	private int					maxMessageLength	= -1;
	private int					dumpBytes			= 256;
	private boolean				isDebugEnabled;
	private XipCodecProvider	provider;
	
	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		if (null != buffer && buffer.readable()) {
			// 读第一个整型数为MsgId
			int msgId = buffer.readInt();
			
			// 读第二个整形数为数据长度
			int offset = buffer.readInt();
			
			byte[] bytes = new byte[offset];
			buffer.readBytes(bytes);
			
			Object obj = provider.decode(bytes, msgId);
			if (LOG.isTraceEnabled()) {
				LOG.trace("decoder obj = [{}]", obj);
			}
			return obj;
		}
		return null;
	}
	
	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}
	
	public int getDumpBytes() {
		return dumpBytes;
	}
	
	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}
	
	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}
	
	public int getMaxMessageLength() {
		return maxMessageLength;
	}
	
	public void setMaxMessageLength(int maxMessageLength) {
		this.maxMessageLength = maxMessageLength;
	}
	
	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(XipCodecProvider provider) {
		this.provider = provider;
	}
	
}
