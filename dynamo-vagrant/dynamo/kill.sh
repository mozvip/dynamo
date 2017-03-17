#!/bin/sh
cd /dynamo/dynamo
if [ -f "dynamo.pid" ]
then
	echo "Killing currently running dynamo process"
	kill -9 `cat dynamo.pid`
fi
