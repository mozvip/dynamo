#!/bin/sh
cd /dynamo/dynamo
if [ -f "dynamo.pid" ]
then
    echo "Killing currently running dynamo process"
    kill -9 `cat dynamo.pid`
fi
wget https://guigui.tech/dynamo-latest.jar
ulimit -c unlimited
java -agentlib:jdwp=transport=dt_socket,server=y,address=6006,suspend=n -jar -server -Djava.awt.headless=true dynamo-latest.jar &
echo $! > dynamo.pid
