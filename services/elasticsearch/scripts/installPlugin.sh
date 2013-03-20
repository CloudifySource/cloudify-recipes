#!/bin/bash


# args:
# $1 installationFolder - The folder that contains the folder in which elasticsearch is installed
# $2 pluginName - The name of the plugin (for example : lukas-vlcek/bigdesk)

installationFolder="$1"
echo "elasticsearch installationFolder is ${installationFolder}"

cd $installationFolder
rootFolderName=`ls`
echo "elasticsearch rootFolderName is ${rootFolderName}"
homeFolder=$installationFolder/$rootFolderName
echo "elasticsearch home folder is ${homeFolder}"

binFolder=${homeFolder}/bin
echo "elasticsearch bin folder is ${binFolder}"

pluginName="$2"
echo "elasticsearch bin folder is ${pluginName}"


# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}


export PATH=$PATH:/usr/sbin:/sbin:$binFolder || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin:${binFolder}"

${binFolder}/plugin -install $pluginName

echo "End of $0"
