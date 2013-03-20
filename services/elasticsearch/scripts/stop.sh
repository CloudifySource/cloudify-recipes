#!/bin/bash -x


# args:
# $1 installationFolder - The folder that contains the folder in which elasticsearch is installed
# $2 httpPort - elasticsearch http port

installationFolder="$1"
httpPort="$2"


function killelasticsearchProcess {
	ps -ef | grep -i "elasticsearch" | grep -viE "grep|gsc|gsa|gigaspaces"
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -i "elasticsearch" | grep -viE "grep|gsc|gsa|gigaspaces" | awk '{print $2}' | xargs sudo kill -9
	fi  
}

homeFolder=${installationFolder}/`ls`
echo "elasticsearch home folder is ${homeFolder}"

binFolder=${homeFolder}/bin
echo "elasticsearch bin folder is ${binFolder}"

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exits with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}


export PATH=$PATH:/usr/sbin:/sbin:$binFolder || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin:${binFolder}"

echo "Killing elasticsearch via REST shutdown ..."
curl -XPOST 'http://localhost:${httpPort}/_shutdown'

echo "Killing elasticsearch if it is still alive after REST shutdown ..."
killelasticsearchProcess

echo "End of $0"
