#!/bin/sh

SERVICE_NAME=dynamo

mkdir /var/log/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/log/$SERVICE_NAME
mkdir /var/lib/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/lib/$SERVICE_NAME
mkdir /var/run/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/run/$SERVICE_NAME
update-rc.d $SERVICE_NAME defaults