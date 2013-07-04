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
package org.driftframework.client;

import java.util.concurrent.TimeUnit;

import org.driftframework.protocol.Xip;
import org.driftframework.protocol.XipRequest;
import org.driftframework.protocol.XipResponse;
import org.driftframework.response.ResponseClosure;

/**
 * definition send role
 * 
 * @author qiaofeng
 * @version $Id: Sender, v 0.1 2013-6-30 下午7:51:05 Exp $
 */
public interface Sender {
	
	<Req extends Xip> void send(final Req req);
	
	<Req extends Xip> void send(final String path, final Req req);
	
	<Req extends XipRequest, Resp extends XipResponse> void send(final Req req,
			ResponseClosure<Resp> callback);
	
	<Req extends XipRequest, Resp extends XipResponse> void send(
			final String path, final Req req, ResponseClosure<Resp> callback);
	
	<Req extends XipRequest, Resp extends XipResponse> Resp sendAndWait(
			final Req req);
	
	<Req extends XipRequest, Resp extends XipResponse> Resp sendAndWait(
			final String path, final Req req);
	
	<Req extends Xip, Resp extends XipResponse> Resp sendAndWait(final Req req,
			long timeout, TimeUnit units);
	
	<Req extends Xip, Resp extends XipResponse> Resp sendAndWait(
			final String path, final Req req, long timeout, TimeUnit units);
	
}
