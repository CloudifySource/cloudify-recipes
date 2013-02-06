#!/bin/bash

# args:
# $1 rpm32FileUrl ( for example : http://packages.couchbase.com/releases/1.8.1/couchbase-server-community_x86_1.8.1.rpm )
# $2 rpm64FileUrl ( for example : http://packages.couchbase.com/releases/1.8.1/couchbase-server-community_x86_64_1.8.1.rpm )

rpm32FileUrl="$1"
rpm64FileUrl="$2"

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exits with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

function stopCouchbaseProcess {
	sudo /etc/init.d/couchbase-server stop 2>&1 > /dev/null
	status=$?
	echo "Couchbase service stopped with status $status"
}
function killCouchbaseProcess {
	ps -ef | grep -i "couchbase" | grep -viE "grep|gsc|gsa|gigaspaces|${0}"
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -i "couchbase" | grep -viE "grep|gsc|gsa|gigaspaces|${0}" | awk '{print $2}' | xargs sudo kill -9
	fi  
}

export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin"

echo "Installing Couchbase on one of the following : Red Hat, CentOS, Fedora, Amazon ..."

echo "Killing previous couchbase installation if exits ..."
killCouchbaseProcess
 
echo "Removing previous couchbase installations if exist..."
sudo rpm -qa | grep -i "couchbase" | sudo xargs rpm -e

echo "Removing potential leftovers after uninstall..."
sudo rm -rf /opt/couchbase || error_exit $? "Failed on: sudo rm -rf /opt/couchbase"

sudo yum -y -q install openssl098e

currLocation=`pwd`

ARCH=`uname -m`
echo "Machine Architecture -- ${ARCH}"
if [ "$ARCH" = "x86_64" ]; then
	rpmFile=$rpm64FileUrl
else 
	rpmFile=$rpm32FileUrl
fi

rm -f *.rpm
echo "wgetting ${rpmFile} ..."
wget $rpmFile
echo "sudo rpm --install ${rpmFile} ..."
ls $currLocation/*.rpm | xargs sudo rpm --install

echo "Stopping couchbase in order to configure the service..."
stopCouchbaseProcess
killCouchbaseProcess

echo "End of $0" 