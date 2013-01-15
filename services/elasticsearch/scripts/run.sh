#!/bin/bash -x


# args:
# $1 installationFolder - The folder that contains the folder in which elasticsearch is installed

installationFolder="$1"
echo "elasticsearch installationFolder is ${installationFolder}"

cd $installationFolder
rootFolderName=`ls`
echo "elasticsearch rootFolderName is ${rootFolderName}"
homeFolder=$installationFolder/$rootFolderName
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

ps -ef | grep -i elasticsearch | grep -viE "grep|gsc|gsa|gigaspaces"

echo "About to invoke ${binFolder}/elasticsearch -f ..."
${binFolder}/elasticsearch -f

echo "End of $0"
