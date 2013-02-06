#!/bin/bash

# args:
# $1 cluster Host 
# $2 cluster Port 
# $3 bucket Name 

clusterHost="$1"
clusterPort="$2"
bucketName="$3"

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

cbstatsCmd="/opt/couchbase/bin/cbstats"

#${cbstatsCmd} $clusterHost:$clusterPort -b $bucketName all  | grep -E "[0-9]" | grep -viE "version|ep_dbname"| awk -F : '{print "\""$1"\":"$2}' | sed -e "s/\" /\"/g" | sed -e "s/ //g"

${cbstatsCmd} $clusterHost:$clusterPort -b $bucketName all  | grep -E "[0-9]" | grep -viE "version|ep_dbname" | sed -e "s/ //g"


