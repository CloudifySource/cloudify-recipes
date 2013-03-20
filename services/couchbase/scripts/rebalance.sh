#!/bin/bash -x

# args:
# $1 cluster Host 
# $2 cluster Port 
# $3 cluster admin (user name)
# $4 cluster (admin's) password
# $5 Bucket Name
# $6 needToLoadData ( only for a demo ) 
# $7 dataPath , only if needToLoadData is "true"


clusterHost="$1"
clusterPort="$2"
clusterAdmin="$3"
clusterPassword="$4"
clusterBucketName="$5"
needToLoadData="$6"
dataPath="$7"


# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exits with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin"

cli="/opt/couchbase/bin/couchbase-cli"


echo "Rebalancing cluster..."	
${cli} rebalance -u $clusterAdmin -p $clusterPassword -c $clusterHost:$clusterPort -d

if  [ "${needToLoadData}" == "true" ] ; then
	#This part is for a demo only and it will be removed once a timeout is added to the invoke custom command
	cbDocLoaderSh="/opt/couchbase/bin/tools/cbdocloader"
	echo "Adding ${dataPath} ... "
	${cbDocLoaderSh} -u $clusterAdmin -p $clusterPassword -n $clusterHost:$clusterPort -b $clusterBucketName $dataPath
fi	

echo "End of $0"
