#!/bin/bash

scriptPos=`dirname $0`

cd "$scriptPos/../vagrant/ubuntu_logserver" && vagrant halt
