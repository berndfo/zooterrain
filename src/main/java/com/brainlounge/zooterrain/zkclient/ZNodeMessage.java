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
package com.brainlounge.zooterrain.zkclient;

import org.apache.zookeeper.data.Stat;

/**
 */
public class ZNodeMessage {
    public static enum Type { C, U, D }

    protected Type type;
    protected String fqZNodeName;
    protected Stat stat;

    public ZNodeMessage(String fqZNodeName, Type type, Stat stat) {
        this.fqZNodeName = fqZNodeName;
        this.type = type;
        this.stat = stat;
    }

    public String toJson() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.
                append("{").
                append("\"type\":").append(quoted(type.toString())).append(",").
                append("\"znode\":").append(quoted(fqZNodeName));
        if (stat != null) {
            stringBuilder.append(",");
            stringBuilder.append("\"eph\":").append(stat.getEphemeralOwner() != 0).append(",");
            stringBuilder.append("\"ct\":").append(stat.getCtime()).append(",");
            stringBuilder.append("\"mt\":").append(stat.getMtime()).append(",");
            stringBuilder.append("\"px\":").append(stat.getPzxid());
        }
        String json = stringBuilder.append("}").toString();
        return json;
    }

    protected String quoted(String notQuoted) {
        return "\"" + notQuoted + "\""; 
    }
}
