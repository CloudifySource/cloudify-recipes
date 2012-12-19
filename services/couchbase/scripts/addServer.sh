#!/bin/bash -x

# args:
# $1 cluster Host 
# $2 cluster Port 
# $3 cluster admin (user name)
# $4 cluster (admin's) password
# $5 new server host
# $6 new server port
# $7 new server user
# $8 new server password

clusterHost="$1"
clusterPort="$2"
clusterAdmin="$3"
clusterPassword="$4"
newServerHost="$5"
newServerPort="$6"
newServerAdmin="$7"
newServerPassword="$8"

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

echo "Adding ${newServerHost}:${newServerPort} to cluster list"
${cli} server-add -u $clusterAdmin -p $clusterPassword -c $clusterHost:$clusterPort --server-add=$newServerHost:$newServerPort  --server-add-username=$newServerAdmin --server-add-password=$newServerPassword -d


echo "Invoking server-list on ${clusterHost}:${clusterPort} "
${cli} server-list -u $clusterAdmin -p $clusterPassword -c $clusterHost:$clusterPort -d


echo "End of $0"
