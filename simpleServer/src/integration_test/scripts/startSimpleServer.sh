#!/bin/bash

scriptPos=`dirname $0`

serverCount=$2

if [ $1='start' ]; then
    cd "$scriptPos/../vagrant/ubuntu_logserver" && vagrant ssh -c "/vagrant/scripts/startSimpleServer.sh start $serverCount"
else
    cd "$scriptPos/../vagrant/ubuntu_logserver" && vagrant ssh -c "/vagrant/scripts/startSimpleServer.sh stop $serverCount"
fi
