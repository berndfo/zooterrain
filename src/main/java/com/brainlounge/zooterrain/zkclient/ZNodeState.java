package com.brainlounge.zooterrain.zkclient;

import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 */
public class ZNodeState {
    
    private final Semaphore access = new Semaphore(1);
    
    protected Set<String> children = Collections.emptySet();
    protected Stat stat = null; 
    
    public ZNodeState() {
    }

    public void acquire() throws InterruptedException {
        access.acquire();
    }
    
    public void release() {
        access.release();
    }

    public Set<String> getChildren() {
        return children;
    }

    public void setChildren(Set<String> newChildren) {
        children = newChildren;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }
}
