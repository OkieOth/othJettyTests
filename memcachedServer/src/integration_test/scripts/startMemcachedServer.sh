#!/bin/bash

scriptPos=`dirname $0`

serverCount=$2

rm -f $scriptPos/../vagrant/ubuntu_memcached/MemcachedServer*.pid

if [ $1='start' ]; then
    cd "$scriptPos/../vagrant/ubuntu_memcached" && vagrant ssh -c "/vagrant/scripts/startMemcachedServer.sh start $serverCount"
else
    cd "$scriptPos/../vagrant/ubuntu_memcached" && vagrant ssh -c "/vagrant/scripts/startMemcachedServer.sh stop $serverCount"
fi
