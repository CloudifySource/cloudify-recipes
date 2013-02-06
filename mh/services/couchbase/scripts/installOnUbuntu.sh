#!/bin/bash -x

# args:
# $1 deb32FileUrl ( for example : http://packages.couchbase.com/releases/1.8.1/couchbase-server-community_x86_1.8.1.deb )
# $2 deb64FileUrl ( for example : http://packages.couchbase.com/releases/1.8.1/couchbase-server-community_x86_64_1.8.1.deb )

deb32FileUrl="$1"
deb64FileUrl="$2"


# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exits with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

function killCouchbaseProcess {
	ps -ef | grep -i "couchbase" | grep -viE "grep|gsc|gsa|gigaspaces"
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -i "couchbase" | grep -viE "grep|gsc|gsa|gigaspaces" | awk '{print $2}' | xargs sudo kill -9
	fi  
}

export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin"

echo "Installing Couchbase on one of the following : Ubuntu, Debian, Mint ..."

echo "Killing previous couchbase installation if exits ..."
killCouchbaseProcess
 
echo "Removing previous couchbase installation if exits..."
#sudo deb uninstall bla bla || error_exit $? "Failed on: sudo deb uninstall ..."

echo "Removing potential leftovers after uninstall..."
sudo rm -rf /opt/couchbase || error_exit $? "Failed on: sudo rm -rf /opt/couchbase"

currLocation=`pwd`


ARCH=`uname -m`
echo "Machine Architecture -- ${ARCH}"
if [ "$ARCH" = "x86_64" ]; then
	debFile=$deb64FileUrl
else 
	debFile=$deb32FileUrl
fi

rm -f *.deb
echo "wgetting ${debFile} ..."
wget $debFile
echo "sudo dpkg -i ${debFile} ..."
ls $currLocation/*.deb | xargs sudo dpkg -i

echo "Stopping couchbase in order to configure the service..."
killCouchbaseProcess

 
echo "End of $0" 