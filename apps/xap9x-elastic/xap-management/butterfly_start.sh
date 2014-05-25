#!/bin/bash


# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
#
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}
source virtenv/bin/activate
python `pwd`/butterfly/butterfly.server.py --host="0.0.0.0" --port="8081" --unsecure --prompt_login=false &
deactivate