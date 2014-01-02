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
