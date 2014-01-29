package com.brainlounge.zooterrain.zkclient;

/**
 */
public interface ClientRequest {
    
    public static enum Type { 
        i, // retrieve all initial data
        b, // retrieve node data
    }
}
