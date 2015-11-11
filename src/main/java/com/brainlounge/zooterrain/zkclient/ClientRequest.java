package com.brainlounge.zooterrain.zkclient;

/**
 */
public interface ClientRequest {
    
    enum Type { 
        i, // retrieve all initial data
        b, // retrieve node data
        u, // update node with new data, create node if not already existant
    }
}
