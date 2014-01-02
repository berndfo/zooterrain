/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.brainlounge.zooterrain.netty;

import com.brainlounge.zooterrain.zkclient.ZNodeMessage;
import com.brainlounge.zooterrain.zkclient.ZkStateListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 */
public class OutboundConnector implements ZkStateListener {
    protected ChannelHandlerContext context;

    public OutboundConnector(ChannelHandlerContext context) {
        this.context = context;
    }

    @Override
    public void zkNodeEvent(ZNodeMessage message) {
        try {
            context.channel().writeAndFlush(new TextWebSocketFrame(message.toJson()), context.newPromise());
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
