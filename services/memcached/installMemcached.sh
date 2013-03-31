#!/bin/bash
#
#	This script installs memcached

echo "About to install memcached ... "

if rpm -qa | grep -q memcached; then
	echo "Memcached is already installed."
else
	sudo yum -y install memcached.x86_64
fi

echo "End of install of Memcached."
