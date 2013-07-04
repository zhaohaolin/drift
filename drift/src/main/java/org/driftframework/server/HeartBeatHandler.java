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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author qiaofeng
 * @version $Id: HeartBeatHandler, v 0.1 2013年7月2日 下午4:06:15 Exp $
 */
public class HeartBeatHandler extends IdleStateAwareChannelHandler {
	
	private final static Logger	LOG	= LoggerFactory
											.getLogger(HeartBeatHandler.class);
	
	private int					i	= 0;
	
	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
			throws Exception {
		super.channelIdle(ctx, e);
		
		// 超过5次没有发送数据，默认为客户端掉线
		if (e.getState() == IdleState.WRITER_IDLE)
			i++;
		
		if (i == 5) {
			e.getChannel().close();
			if (LOG.isWarnEnabled()) {
				LOG.warn("channel=[{}] is less the connection.", e.getChannel()
						.getId());
			}
		}
	}
	
}
