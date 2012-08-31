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

export PATH=$PATH:/usr/sbin:/sbin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin"

sudo `which httpd` -k stop || error_exit $? "Failed on: sudo which httpd -k stop"


echo "Using yum. Uninstalling on one of the following : Red Hat, CentOS, Fedora, Amazon"
sudo yum remove -y -q php5* php*
sudo yum remove -y -q httpd || error_exit $? "Failed on: sudo yum remove -y -q httpd"
sudo rm -rf  /etc/php* || error_exit $? "Failed on: sudo rm -rf  /etc/php*"
sudo rm -rf  /usr/bin/php* || error_exit $? "Failed on: sudo rm -rf  /usr/bin/php"
sudo rm -rf  /usr/share/php* || error_exit $? "Failed on: sudo rm -rf /usr/share/php"

# The following two statements are used since in some cases, there are leftovers after uninstall
sudo rm -rf /etc/httpd || error_exit $? "Failed on: sudo rm -rf /etc/httpd"
sudo rm -rf /usr/sbin/httpd || error_exit $? "Failed on: sudo rm -rf /usr/sbin/httpd"




