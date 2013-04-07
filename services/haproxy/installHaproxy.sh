#!/bin/bash
#
#	This script installs haproxy

echo "About to install haproxy ... "

function killYumUpdatesd {
	updatesdPid=`ps -ef | grep /usr/sbin/yum-updatesd | grep -v grep | awk '{print $2}'`
	echo ${updatesdPid}
	if [ ! -z ${updatesdPid} ]; then 
		kill -9 ${updatesdPid}
	fi
}

echo "Kill yum-updatesd if it is running."
killYumUpdatesd

echo "http_proxy is ${http_proxy}"

if rpm -qa | grep -q epel-release; then
	echo "epel is already installed"
else
	wget -O /tmp/epel-release-6-8.noarch.rpm http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm
	rpm -Uvh /tmp/epel-release-6-8.noarch.rpm
fi

if rpm -qa | grep -q haproxy; then
	echo "haproxy is already installed"
else
	yum -y -v install haproxy
fi

echo "End of install of haproxy."