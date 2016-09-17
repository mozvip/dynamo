#!/bin/sh

SERVICE_NAME=dynamo

cp etc/default/$SERVICE_NAME /etc/default/$SERVICE_NAME
[ -r /etc/default/$SERVICE_NAME ] && . /etc/default/$SERVICE_NAME

cp etc/init.d/$SERVICE_NAME /etc/init.d/$SERVICE_NAME
chmod +x /etc/init.d/$SERVICE_NAME

mkdir /var/log/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/log/$SERVICE_NAME
mkdir $SERVICE_HOME
chown $SERVICE_USER:$SERVICE_GROUP $SERVICE_HOME
mkdir /var/run/$SERVICE_NAME
chown $SERVICE_USER:$SERVICE_GROUP /var/run/$SERVICE_NAME
update-rc.d $SERVICE_NAME defaults