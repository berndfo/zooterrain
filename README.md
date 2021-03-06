zooterrain
==========

View ZooKeeper znode tree in a browser  

zooterrain is a small server every WebSocket-capable browser is able to connect to.
It will display all the znodes from a running ZooKeeper ensemble.
As znodes are deleted or new ones are created, this will be visualized in the browser.

## Features

1. Display changes to the tree as they happen (new, removed nodes)
1. Display changes to the data of each node
1. Filter nodes by name
1. Ignore nodes (and their children)
1. Watch nodes
1. Display a node's attached data, also as a hex dump

## Building

git clone the repository and run Maven

    mvn install

## Running

In order for zooterrain to connect to a ZooKeeper, you need to know the address of one 
ZooKeeper instance you want to track, which typically looks much like this:
 
    myzookeeper.mycomp.com:2181

and then start the application the by running

    java -cp target/zooterrain-full.jar -Dzooterrain.conn=${ZOOKEEPER_HOST_PORT} com.brainlounge.zooterrain.netty.WebSocketServer 8080

Be sure to always replace ${ZOOKEEPER_HOST_PORT} with your ZooKeeper address.
or simply:

    java -Dzooterrain.conn=${ZOOKEEPER_HOST_PORT} -jar target/zooterrain-full.jar 

## Connecting from Browser    
    
Open a browser and navigate to http://localhost:8080/
You can run the server on other ports by changing the last parameter (here: 8080) in the java command line.

