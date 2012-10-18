#!/bin/bash -x

# args:
# $1 The original port in the apache2.conf (ususally 80)
# $2 The required port in the apache2.conf
# $3 need php or not ? "true" or "false" (string, not boolean)
# $4 applicationZipUrl full path to the zip file
# $5 zipContentLevel - Can be either "0" or "1" ("0" means that the content is in the root level folder)
# 

origPort=$1
newPort=$2
needPhp="$3"
applicationZipUrl="$4"
zipContentLevel="$5"

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}


export PATH=$PATH:/usr/sbin:/sbin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin"

docRoot="/var/www"

apache2Location=`whereis apache2`
for i in ${apache2Location}
do    
	if [ -d "$i" ] ; then
		portsConf="$i/ports.conf"		
		if [ -f "${portsConf}" ] ; then
			echo "portsConf is in ${portsConf}"					
			echo "Replacing $origPort with $newPort..."
			sudo sed -i -e "s/$origPort/$newPort/g" ${portsConf} || error_exit $? "Failed on: sudo sed -i -e $origPort/$newPort in ${portsConf}"			
			echo "End of ${portsConf} replacements"
								
			defaultFile="$i/sites-available/default"			
			sudo sed -i -e "s/$origPort/$newPort/g" ${defaultFile} || error_exit $? "Failed on: sudo sed -i -e $origPort/$newPort in ${defaultFile}"
			sudo sed -i -e "s/AllowOverride None/AllowOverride All/g" ${defaultFile} || error_exit $? "Failed on: sudo sed -i -e AllowOverride None/AllowOverride All in ${defaultFile}"
					
			if  [ "${needPhp}" == "true" ] ; then
				sudo echo "<?php phpinfo(); ?>" >> $docRoot/index.php  
			fi 
			
			sudo a2enmod rewrite
			
			if  [ "${applicationZipUrl}" != "NOT_REQUIRED" ] ; then
			  echo "Deleting ${docRoot}/index.html ..."
			  sudo rm -rf $docRoot/index.html
			  echo "Creating tmpZipFolder..."
			  mkdir tmpZipFolder
			  cd tmpZipFolder
			  echo "wgetting ${applicationZipUrl}"
			  wget $applicationZipUrl
			  ls *.zip | xargs echo "Unzipping"
			  if  [ "${zipContentLevel}" == "0" ] ; then
				zipLocation=`pwd`
				cd $docRoot
				rm -rf index.php
				echo "Unzipping to ${docRoot} ... " 
				ls $zipLocation/*.zip | xargs sudo unzip -o
				rm $zipLocation/*.zip  
			  else
				ls *.zip | xargs sudo unzip -o
				rm *.zip
			  fi
			  
			  if  [ "${zipContentLevel}" != "0" ] ; then
				echo "mv unzipped folder to appFolder"
				ls | xargs -I file mv file appFolder
				cd appFolder
				echo "Copying application files to ${docRoot}/ ..."
				ls | xargs -I file sudo cp -r file $docRoot/  
				cd ..
				rm -rf tmpZipFolder
			  else
				echo "The content of ${applicationZipUrl} is the root folder of the zip file"
			  fi				
			fi  
			echo "End of $0" 
			exit 0
		fi	
    fi	
done

echo "End of $0" 


