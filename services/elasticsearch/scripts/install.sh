#!/bin/bash -x

# args:
# $1 elasticsearchZip ( for example : http://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-0.20.1.zip )
# $2 installationFolder - The folder to which the zip files should be downloaded and extracted
# $3 newHttpPort - http port
# $4 node2NodePort - node to node communication port
# $4 unicastsHosts - a comma separated list of host addresses ( e.g. : 123.44.5.6,556.56.7.23)

elasticsearchZip="$1"
installationFolder="$2"
newHttpPort="$3"
node2NodePort="$4"
unicastsHosts="$5"

echo "elasticsearchZip is ${elasticsearchZip}"
echo "installationFolder is ${installationFolder}"
echo "httpPort is ${httpPort}"
echo "node2NodePort is ${node2NodePort}"
echo "unicastsHosts is ${unicastsHosts}"


# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exits with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

function killelasticsearchProcess {
	ps -ef | grep -i "elasticsearch" | grep -viE "grep|gsc|gsa|gigaspaces"
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -i "elasticsearch" | grep -viE "grep|gsc|gsa|gigaspaces" | awk '{print $2}' | xargs sudo kill -9
	fi  
}

export PATH=$PATH:/usr/sbin:/sbin:$installationFolder || error_exit $? "Failed on: export PATH=${PATH}:/usr/sbin:/sbin:${$installationFolder}"

type apt-get
useYum=$?

type unzip
if [ $? -ne 0 ] ; then 
	if [ $useYum -ne 0 ] ; then 
		sudo yum install -y -q unzip
	else
		sudo apt-get install -y -q unzip
	fi
fi


echo "Killing previous elasticsearch installation if exists ..."
killelasticsearchProcess

echo "Removing potential leftovers from previous installations..."
sudo rm -rf $installationFolder || error_exit $? "Failed on: sudo rm -rf ${installationFolder}"

mkdir -p $installationFolder

cd $installationFolder
echo "wgetting ${elasticsearchZip} ..."
wget $elasticsearchZip

echo "ls -l *.zip ..."
ls -l *.zip 
echo "unzipping ${elasticsearchZip} ..."
ls *.zip | xargs unzip
echo "Deleting ${elasticsearchZip} ..."
rm -f *.zip

rootFolderName=`ls`
echo "elasticsearch rootFolderName is ${rootFolderName}"
homeFolder=$installationFolder/$rootFolderName
echo "elasticsearch home folder is ${homeFolder}"

confFolder=${homeFolder}/config
echo "elasticsearch config folder is ${confFolder}"

esYml=$confFolder/elasticsearch.yml
echo "elasticsearch yml is ${esYml}"

# The following is a list of properties that need to be changed in the elasticsearch.yml prior to ES start.
# All these properties and values are commented by default, so even if their values are correct, 
# we keep the original comment and set a new and valid value.
httpPortStr="http.port"
transportStr="transport.tcp.port"
multicastStr="discovery.zen.ping.multicast.enabled"
unicastStr="discovery.zen.ping.unicast.hosts"
propertiesNames=( $httpPortStr $transportStr $multicastStr $unicastStr) 

unicastList=`echo [\"$unicastsHosts:$node2NodePort\"]| sudo sed  "s/,/:$node2NodePort\",\"/g"`


# These are the new values
newValues=( $newHttpPort $node2NodePort "false" "$unicastList")

# Set the new values in elasticsearch.yml
for (( i = 0 ; i < ${#propertiesNames[@]} ; i++ )) do
	currProperty="${propertiesNames[$i]}"
	currNewValue="${newValues[$i]}"
	origString="# ${currProperty}:"
	newString="${currProperty}: ${currNewValue}\n${origString}"
	
	echo "Setting ${currProperty} to ${currNewValue}..."
	sudo sed -i -e "s/$origString/$newString/g" $esYml
	echo "====================================================="
done

# set the unicast ...
#discovery.zen.ping.unicast.hosts: ["10.214.102.67:9300", "10.201.202.80:9300"]

echo "End of $0" 