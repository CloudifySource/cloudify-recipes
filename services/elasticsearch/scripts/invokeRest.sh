#!/bin/bash


# args:
# $1 installationFolder - The folder that contains the folder in which elasticsearch is installed
# $2 currentHost - Current host ( usually localhost )
# $3 httpPort - elasticsearch http port
# $4 restMethod -  Rest method : POST ,GET or PUT
# $5 currCommand - current command
# $6 commandArgs - current command's args. E.G. :  "user": "Mike", "message": "Good one" which will be wrapped with -d '{ ... } '

installationFolder="$1"
currentHost="$2"
httpPort="$3"
restMethod="$4"
currCommand="$5"
commandArgs="`echo $6 | sed 's/ /%20/g'`"
		
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
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}


export PATH=$PATH:/usr/sbin:/sbin:$binFolder || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin:${binFolder}"

case $restMethod in
    "PUT")
      echo "curl -X${restMethod} http://${currentHost}:${httpPort}/${currCommand} -d $commandArgs"
      curl -X$restMethod http://$currentHost:$httpPort/$currCommand -d $commandArgs
      ;;
    "GET")
      echo "curl -X${restMethod} http://${currentHost}:${httpPort}/${currCommand}"
      curl -X$restMethod http://$currentHost:$httpPort/$currCommand
      ;;         
    "POST")
      echo "curl -X${restMethod} 'http://${currentHost}:${httpPort}/${currCommand}'"
      curl -X$restMethod 'http://$currentHost:$httpPort/$currCommand'
      ;; 	  
    *)
      echo "curl -X${restMethod} 'http://${currentHost}:${httpPort}/${currCommand}'"
      curl -X$restMethod 'http://$currentHost:$httpPort/$currCommand'
      ;; 	  
esac

echo " - "
echo "End of $0"
