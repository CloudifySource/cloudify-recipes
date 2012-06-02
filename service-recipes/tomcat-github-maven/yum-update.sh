#! /bin/bash


# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

#sudo yum -y update --security || error_exit $? "Failed to update yum"
sudo yum -y install git || error_exit $? "Failed to install git"