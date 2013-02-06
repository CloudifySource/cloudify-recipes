#!/bin/bash -x

# args:
# $1 cluster Port 
# $2 cluster admin (user name)
# $3 cluster (admin's) password
# $4 local bucket name
# $5 remote cluster reference
# $6 remote cluster node public dns name
# $7 remote cluster port
# $8 remote cluster user
# $9 remote cluster password
# ${10} remote bucket name
# ${11} replication type

clusterPort="$1"
clusterAdmin="$2"
clusterPassword="$3"
localBucketName="$4"
remoteClusterRefName="$5"
remoteClusterNodeName="$6"
remoteClusterPort="$7"
remoteClusterUser="$8"
remoteClusterPassword="$9"
remoteBucketName="${10}"
replicationType="${11}"

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

export publicHostName="`wget -q -O - http://169.254.169.254/latest/meta-data/public-hostname`"
echo "Node public hostname    - ${publicHostName}"
echo " clusterPort            - ${clusterPort}"
echo " clusterAdmin           - ${clusterAdmin}"
echo " clusterPassword        - ${clusterPassword}"
echo " localBucketName        - ${localBucketName}"
echo " remoteClusterRefName   - ${remoteClusterRefName}"
echo " remoteClusterNodeName  - ${remoteClusterNodeName}"
echo " remoteClusterPort      - ${remoteClusterPort}"
echo " remoteClusterUser      - ${remoteClusterUser}"
echo " remoteClusterPassword  - ${remoteClusterPassword}"
echo " remoteBucketName       - ${remoteBucketName}"
echo " replicationType        - ${replicationType}"

echo "Creating Cluster Reference ${remoteClusterRefName} with the cluster node ${remoteClusterNodeName} using the command - "
echo "curl -v -u ${clusterAdmin}:${clusterPassword} http://${publicHostName}:${clusterPort}/pools/default/remoteClusters -d name=${remoteClusterRefName} -d hostname=${remoteClusterNodeName}:${remoteClusterPort} -d username=${remoteClusterUser} -d password=${remoteClusterPassword}"

export uuid="`curl -v -u ${clusterAdmin}:${clusterPassword} http://${publicHostName}:${clusterPort}/pools/default/remoteClusters -d name=${remoteClusterRefName} -d hostname=${remoteClusterNodeName}:${remoteClusterPort} -d username=${remoteClusterUser} -d password=${remoteClusterPassword} | python -c 'import json,sys;obj=json.load(sys.stdin);print obj["uuid"]'`"

echo "Create Cluster Reference UUID was - ${uuid}" 

echo "Creating Replication with ${remoteClusterRefName} using the command - "
"curl -v -X POST -u ${clusterAdmin}:${clusterPassword} http://${publicHostName}:${clusterPort}/controller/createReplication -d uuid=${uuid} -d fromBucket=${localBucketName} -d toCluster=${remoteClusterRefName} -d toBucket=${remoteBucketName} -d replicationType=${replicationType}"
export createReplicationJSON="`curl -v -X POST -u ${clusterAdmin}:${clusterPassword} http://${publicHostName}:${clusterPort}/controller/createReplication -d uuid=${uuid} -d fromBucket=${localBucketName} -d toCluster=${remoteClusterRefName} -d toBucket=${remoteBucketName} -d replicationType=${replicationType}`"

echo "Create Replication returned - ${createReplicationJSON}"

echo "End of $0"
