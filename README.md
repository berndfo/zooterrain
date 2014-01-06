zooterrain
==========

View ZooKeeper znode tree in a browser  

zooterrain is a small server every WebSocket-capable browser is able to connect to.
It will display all the znodes from a running ZooKeeper ensemble.
As znodes are deleted or new ones are created, this will be visualized in the browser.

## Building

git clone the repository and run Maven

    mvn install

## Running

In order for zooterrain to connect to a ZooKeeper, you need to know the address of one 
ZooKeeper instance you want to track, which typically looks much like this:
 
    myzookeeper.mycomp.com:2181

and then start the application the by running

    java -cp target/zooterrain-full.jar -Dzooterrain.conn=${ZOOKEEPER_HOST_PORT} com.brainlounge.zooterrain.netty.WebSocketServer 9080  

(replacing ${ZOOKEEPER_HOST_PORT} with your ZooKeeper address).

Open a browser and navigate to http://localhost:9080/
You can run the server on other ports by changing the last parameter (here: 9080) in the java command line.

