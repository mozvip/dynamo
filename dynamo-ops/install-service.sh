#!/bin/sh

SERVICE_NAME=dynamo
SERVICE_USER=dynamo
SERVICE_GROUP=dynamo

cp etc/init.d/$SERVICE_NAME /etc/init.d/$SERVICE_NAME
cp etc/default/$SERVICE_NAME /etc/default/$SERVICE_NAME
chmod +x /etc/init.d/$SERVICE_NAME

mkdir /var/log/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/log/$SERVICE_NAME
mkdir /var/lib/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/lib/$SERVICE_NAME
mkdir /var/run/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/run/$SERVICE_NAME
update-rc.d $SERVICE_NAME defaults