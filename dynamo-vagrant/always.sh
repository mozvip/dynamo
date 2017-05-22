#!/usr/bin/env bash

cd /dynamo/dynamo

# configure beet
cp /vagrant/etc/beets/config.yaml /home/ubuntu/.config/beets/config.yaml

./start.sh &
