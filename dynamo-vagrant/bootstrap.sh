#!/usr/bin/env bash

apt-get update
apt-get install -y openjdk-8-jdk
apt-get install -y nginx
apt-get install -y mediainfo
apt-get install -y transmission-daemon
apt-get install -y sabnzbdplus
apt-get install -y python-pip
pip install --upgrade pip
pip install beets

# configure transmission
service transmission-daemon stop
cp /vagrant/etc/transmission-daemon/settings.json /etc/transmission-daemon/settings.json
service transmission-daemon start

adduser --system sabnzbd --disabled-login --disabled-password
adduser --system dynamo --disabled-login --disabled-password

cp /vagrant/etc/default/sabnzbdplus /etc/default/sabnzbdplus
service sabnzbdplus start

cd /dynamo/dynamo
wget -q https://github.com/glowroot/glowroot/releases/download/v0.9.12/glowroot-0.9.12-dist.zip
unzip glowroot-0.9.12-dist.zip
