#!/bin/bash

# this script starts in the vagrant vm 100 instances of SimpleServer

scriptPos=${0%/*}
scriptName=${0##*/}

MAX_VALUE=100
COUNTER=0
START_PORT=9001

#PATH_TO_PROG=/vagrant/tmp
PATH_TO_PROG=$scriptPos/../../../../../build/libs


STOP=1

PID_FILE=${scriptName%.sh}.pid


function cleanUp {
    	# remove a existing pid file
    	if [ -f "$PID_FILE" ]; then
		rm -f "$PID_FILE"
    	fi
}
trap cleanUp EXIT


if [ "$#" -ne 1 ]; then
    	echo "illegal number of parameters, usage: $scriptName [start|stop]"
	exit 1
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
        echo "wrong parameter, usage: $scriptName [start|stop]"
	exit 1
esac

while [  $COUNTER -lt $MAX_VALUE ]; do
	let START_PORT=START_PORT+1

	if ! [ $STOP -eq 1 ]; then
		# do start the program
		echo "start SimpleServer on port $START_PORT"
		screen -S SimpleServer_$START_PORT -d -m  java -jar $PATH_TO_PROG/simpleServer-all-0.1-SNAPSHOT.jar -p $START_PORT
	else
		# do stop the program
                echo "stop SimpleServer on port $START_PORT"
		screen -X -S SimpleServer_$START_PORT quit
	fi
	let COUNTER=COUNTER+1
done
