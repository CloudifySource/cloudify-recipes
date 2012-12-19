#!/bin/bash -x

# args:
# $1 cluster Host 
# $2 cluster new Port 
# $3 cluster admin (user name)
# $4 cluster (admin's) password
# $5 cluster ram size (in MB)
# $6 postStartRequired ( "true" or "false" ) 
# $7 instanceID
# $8 cluster bucket type
# $9 cluster replica count

clusterHost="$1"
clusterPort="$2"
clusterAdmin="$3"
clusterPassword="$4"
clusterRamSize="$5"
postStartRequired="$6"
instanceID=$7
clusterBucketType="$8"
clusterReplicatCount=$9

clusterBucketName="CloudifyCouchbase${instanceID}"		


# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exits with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

function performPostStart {

	echo "In performPostStart ... "
	counter=`ps -ef |grep -i "couchbase" | grep -viE "grep|gsc|gsa|gigaspaces" | wc -l`

	requiredProcesses=6
	while [ $counter -lt $requiredProcesses ]
	do
	  echo "Available Couchbase processes $counter / $requiredProcesses ..."
	  sleep 5s
	  counter=`ps -ef |grep -i "couchbase" | grep -viE "grep|gsc|gsa|gigaspaces" | wc -l`
	done

	echo "Couchbase is now ready for action"

	$1 cluster-init -c $clusterHost:8091 --cluster-init-username=$clusterAdmin --cluster-init-password=$clusterPassword --cluster-init-port=8091 --cluster-init-ramsize=$clusterRamSize -d	
	
	if [ $instanceID -eq 1 ]; then 
		# This is the 1st machine
		memory_bytes=`cat /proc/meminfo |grep -i memtotal |awk '{print $2;}'`
		memory_mb=`echo "scale=4; ${memory_bytes}/1024" | bc`
		memory_allocation=`echo "scale=4; ${memory_mb}*.79" | bc|cut -d . -f1`
	
		memory_allocation=256
		echo "Memory bytes: ${memory_bytes}"
		echo "Memory MB: ${memory_mb}"
		echo "Memory Allocation: ${memory_allocation}"
	
	
		$1 bucket-create -u $clusterAdmin -p $clusterPassword -c $clusterHost:8091 --bucket=$clusterBucketName --bucket-type=$clusterBucketType --bucket-ramsize=$memory_allocation --bucket-replica=$clusterReplicatCount
		    
	
		echo "Server list:"
		$1 server-list -u $clusterAdmin -p $clusterPassword -c $clusterHost:8091
			
	else
		couchbaseHomeDir=`cat /etc/passwd | cut -f 1,6 -d : | grep couchbase | cut -f 2 -d:`	
		echo "couchbase home dir is ${couchbaseHomeDir}"	
	    dataFolder="${couchbaseHomeDir}/dataFolder"
        echo "dataFolder is ${dataFolder}"	
		$1 node-init -u $clusterAdmin -p $clusterPassword -c $clusterHost:8091 --node-init-data-path=$dataFolder	 
	fi
	
	echo "Changing cluster to listen on port ${couchbase_port}..."
	$1 cluster-init -u $clusterAdmin -p $clusterPassword -c $clusterHost:8091 --cluster-init-port=$clusterPort		

	echo "End of performPostStart"
}

export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin:/opt/couchbase/bin"

cli="/opt/couchbase/bin/couchbase-cli"

sudo /etc/init.d/couchbase-server start

ps -ef | grep -i couchbase | grep -viE "grep|gsc|gsa|gigaspaces"

if  [ $postStartRequired == "true" ]; then 
	echo "Go to performPostStart ..."
	performPostStart $cli
	echo "After performPostStart"
fi


ps -ef | grep -i couchbase | grep -viE "grep|gsc|gsa|gigaspaces"

echo "End of $0"
