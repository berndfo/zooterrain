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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Generates the demo HTML page which is served at http://localhost:8080/
 */
public final class WebSocketServerJSPage {

    private String content = "error loading page";
    
    public WebSocketServerJSPage() throws IOException {
        final InputStream resourceAsStream = WebSocketServerJSPage.class.getResourceAsStream("/hexdump.js");
        content = IOUtils.toString(resourceAsStream, "UTF-8");
    }
    
    public ByteBuf getContent(String webSocketLocation) {
        String contentReplaced = content.replace("${wsLoc}", webSocketLocation);
        return Unpooled.copiedBuffer(contentReplaced, CharsetUtil.US_ASCII);
    }

}
