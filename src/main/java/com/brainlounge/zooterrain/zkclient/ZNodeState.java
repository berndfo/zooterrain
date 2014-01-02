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
