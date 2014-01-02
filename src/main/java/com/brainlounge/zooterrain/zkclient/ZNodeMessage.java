package com.brainlounge.zooterrain.zkclient;

/**
 */
public class ZNodeMessage {
    public static enum Type { C, U, D }

    protected Type type;
    protected String fqZNodeName;

    public ZNodeMessage(String fqZNodeName, Type type) {
        this.fqZNodeName = fqZNodeName;
        this.type = type;
    }

    public String toJson() {
        String json = new StringBuilder().
                append("{").
                append("\"type\":").append(quoted(type.toString())).append(",").
                append("\"znode\":").append(quoted(fqZNodeName)).
                append("}").toString();
        return json;
    }

    protected String quoted(String notQuoted) {
        return "\"" + notQuoted + "\""; 
    }
}
