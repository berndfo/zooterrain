package com.brainlounge.zooterrain.zkclient;

/**
 */
public abstract class ClientMessage {
    public abstract String toJson();

    protected String quoted(String notQuoted) {
        return "\"" + notQuoted + "\""; 
    }
}
