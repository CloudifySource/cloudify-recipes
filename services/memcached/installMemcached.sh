#!/bin/bash
#
#	This script installs memcached

echo "About to install memcached ... "

if rpm -qa | grep -q memcached; then
	echo "Memcached is already installed."
else
	sudo yum -y install memcached.x86_64
	sudo yum list memcache*
	if [ $? -eq 1 ]; then
		# Nothing was installed
		wget http://dl.fedoraproject.org/pub/epel/5/x86_64/epel-release-5-4.noarch.rpm
		rpm -Uvh epel-release*rpm
		sudo yum -y install memcached.x86_64
		sudo yum list memcache*
		if [ $? -eq 1 ]; then
			# Nothing was installed
			sudo yum -y install memcached
		fi			
	fi
fi

echo "End of install of Memcached."
