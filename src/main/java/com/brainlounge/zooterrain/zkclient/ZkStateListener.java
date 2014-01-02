package com.brainlounge.zooterrain.zkclient;

/**
 */
public interface ZkStateListener {
    public void zkNodeEvent(ZNodeMessage message);
}
