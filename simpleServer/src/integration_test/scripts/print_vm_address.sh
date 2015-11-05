#!/bin/bash

scriptPos=`dirname $0`

pushd "$scriptPos/../vagrant/ubuntu_logserver" > /dev/null && vagrant ssh -c "ifconfig | grep inet\ addr: | grep Bcast | awk '{print \$2}' | sed 's/addr://'" | tee run/ip_address.txt && popd > /dev/null




