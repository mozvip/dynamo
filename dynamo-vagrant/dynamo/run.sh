#!/bin/sh
cd /dynamo/dynamo
if [ -f "dynamo.pid" ]
then
    echo "Killing currently running dynamo process"
    kill -9 `cat dynamo.pid`
fi
ulimit -c unlimited
java -agentlib:jdwp=transport=dt_socket,server=y,address=6006,suspend=n -javaagent:/dynamo/dynamo/glowroot/glowroot.jar -jar -server -Djava.awt.headless=true dynamo-core-0.0.1-SNAPSHOT.jar &
echo $! > dynamo.pid
