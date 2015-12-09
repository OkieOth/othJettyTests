#!/bin/bash

# this script starts a number of different MemcachedServer instanzes. All these servers
# echange data with help of a memcached server

scriptPos=${0%/*}
scriptName=${0##*/}

MAX_VALUE=10
COUNTER=0
START_PORT=9001

PATH_TO_PROG=/vagrant/tmp

STOP=1

PID_FILE=${scriptName%.sh}.pid

JAR_NAME=`ls /vagrant/tmp/memcachedServer-all-*.jar`
JAR_NAME=${JAR_NAME##*/}

function cleanUp {
    	# remove a existing pid file
    	if [ -f "$PID_FILE" ]; then
		rm -f "$PID_FILE"
    	fi
}
trap cleanUp EXIT


if [ "$#" -lt 1 ]; then
    	echo "illegal number of parameters, usage: $scriptName [start|stop] [NUMBER_OF_INSTACES]"
	exit 1
fi

if [ "$#" -gt 1 ]; then
	MAX_VALUE=$2
fi

case $1 in
	start)
		if [ -f "$PID_FILE" ]; then
			echo "pid file '$PID_FILE' exists, does the process already run?"
			exit 1
		fi
		STOP=0
		;;
	stop)
		STOP=1
		;;
	*)
        echo "wrong parameter, usage: $scriptName [start|stop] [NUMBER_OF_INSTACES]"
	exit 1
esac

pushd $scriptPos/.. >> /dev/null

while [ $COUNTER -lt $MAX_VALUE ]; do
	if ! [ $STOP -eq 1 ]; then
		# do start the program
		echo `pwd`
		echo "start MemcachedServer on port $START_PORT"
		if ! screen -S MemcachedServer_$START_PORT -d -m  java -jar $PATH_TO_PROG/$JAR_NAME -p $START_PORT; then
			popd
			exit 1
		fi
	else
		# do stop the program
                echo "stop MemcachedServer on port $START_PORT"
		if ! screen -X -S MemcachedServer_$START_PORT quit; then
			popd
			exit 1
		fi
	fi
	let START_PORT=START_PORT+1
	let COUNTER=COUNTER+1
done
popd >> /dev/null
