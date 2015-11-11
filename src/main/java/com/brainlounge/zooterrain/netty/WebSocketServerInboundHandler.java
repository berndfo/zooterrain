/*
 * Copyright 2012 The Netty Project
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
package com.brainlounge.zooterrain.netty;

import com.brainlounge.zooterrain.zkclient.ClientMessage;
import com.brainlounge.zooterrain.zkclient.ClientRequest;
import com.brainlounge.zooterrain.zkclient.ControlMessage;
import com.brainlounge.zooterrain.zkclient.DataMessage;
import com.brainlounge.zooterrain.zkclient.ZkStateListener;
import com.brainlounge.zooterrain.zkclient.ZkStateObserver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import org.apache.zookeeper.CreateMode;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerInboundHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = Logger.getLogger(WebSocketServerInboundHandler.class.getName());

    private static final String WEBSOCKET_PATH = "/firehose";

    protected WebSocketServerHandshaker handshaker;
    protected ZkStateObserver zkStateObserver;
    
    protected ObjectMapper jsonMapper = new ObjectMapper();

    public WebSocketServerInboundHandler(ZkStateObserver zkStateObserver) {
        this.zkStateObserver = zkStateObserver;
    }

    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the demo page and favicon.ico
        if ("/favicon.ico".equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        } else if ("/hexdump.js".equals(req.getUri())) {
            final WebSocketServerJSPage webSocketServerJSPage = new WebSocketServerJSPage();
            ByteBuf content = webSocketServerJSPage.getContent(getWebSocketLocation(req));
            sendHttpResponse(ctx, req, content, "text/javascript; charset=UTF-8");
            return;
        } else if ("/".equals(req.getUri())) {
            WebSocketServerIndexPage webSocketServerIndexPage = new WebSocketServerIndexPage();
            ByteBuf content = webSocketServerIndexPage.getContent(getWebSocketLocation(req));
            sendHttpResponse(ctx, req, content, "text/html; charset=UTF-8");
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            return;
        } 
        
        try {
            handshaker.handshake(ctx.channel(), req);
        } catch (Exception e) {
            logger.log(Level.WARNING, "websocket handshake failed", e);
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            return;
        }
        zkStateObserver.addListener(new OutboundConnector(ctx));
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, ByteBuf content, String contentType) {
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

        res.headers().set(CONTENT_TYPE, contentType);
        setContentLength(res, content.readableBytes());

        sendHttpResponse(ctx, req, res);
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        String request = ((TextWebSocketFrame) frame).text();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("%s received %s", ctx.channel(), request));
        }
        
        // expecting JSON, parse now
        TreeNode jsonRequest;
        ClientRequest.Type requestType;
        try {
            final JsonParser parser = jsonMapper.getFactory().createParser(request);
            jsonRequest = parser.readValueAsTree();
            requestType = ClientRequest.Type.valueOf(readTextValue(jsonRequest, "r"));
        } catch (Exception e) {
            logger.info("parsing JSON failed for '" + request + "'");
            // TODO return error to client
            return;
        }

        if (requestType == null) {
            logger.info("parsing JSON failed, no 'r' field");
            return;
        }

        switch(requestType) {
            case i:
                retrieveInitialData(ctx);
                break;
            case b:
                retrieveNodesData(ctx, jsonRequest);
                break;
            case u:
                String znode = readTextValue(jsonRequest, "z");
                String data = readTextValue(jsonRequest, "d");
                setNodeData(znode, data);
                break;
            default:
                System.out.println("unknown, unhandled client request = " + request);
        }
    }

    private void setNodeData(String znode, String data) {
        try {
            zkStateObserver.getZk().create(znode, data.getBytes(), null, CreateMode.PERSISTENT);
            return;
        } catch (Throwable e) {
            logger.warning("failed to create new node '" + znode + "': " + e.getMessage());
        }
        try {
            final DataMessage dataMessage = zkStateObserver.retrieveNodeData(znode);
            zkStateObserver.getZk().setData(znode, data.getBytes(), dataMessage.getStat().getVersion());
        } catch (Throwable e) {
            logger.warning("failed to update data for " + znode);
        }
    }

    private String readTextValue(TreeNode jsonRequest, final String node) {
        TextNode type = (TextNode)jsonRequest.get(node);
        if (type == null) return null;
        String value = type.textValue();
        return value;
    }

    private void retrieveNodesData(ChannelHandlerContext ctx, TreeNode jsonRequest) {
        final String znode;
        try {
            znode = ((TextNode)jsonRequest.get("z")).textValue();
        } catch (Exception e) {
            return; // TODO return error
        }
        final DataMessage dataMessage = zkStateObserver.retrieveNodeData(znode);
        if (dataMessage != null) {
            writeClientMessage(ctx, dataMessage);
        }
    }

    private void retrieveInitialData(ChannelHandlerContext ctx) {
        // client requested initial data

        // sending connection string info
        final ControlMessage handshakeInfo = new ControlMessage(zkStateObserver.getZkConnection(), ControlMessage.Type.H);
        writeClientMessage(ctx, handshakeInfo);
        final ControlMessage zkInfo = new ControlMessage(zkStateObserver.getStatus(), ControlMessage.Type.Z);
        writeClientMessage(ctx, zkInfo);

        // sending initial znodes
        try {
            zkStateObserver.initialTree("/", 6, Sets.<ZkStateListener>newHashSet(new OutboundConnector(ctx)));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void writeClientMessage(ChannelHandlerContext ctx, ClientMessage clientMessage) {
        ctx.channel().writeAndFlush(new TextWebSocketFrame(clientMessage.toJson()));
    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        messageReceived(channelHandlerContext, o);
    }
}
