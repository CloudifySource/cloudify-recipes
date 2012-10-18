#!/bin/bash

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

httpdLocation=`whereis httpd`
for i in ${httpdLocation}
do    
	if [ -d "$i" ] ; then
		currConf="$i/conf/httpd.conf"		
		if [ -f "${currConf}" ] ; then
			echo "Conf is in ${currConf}"
			echo "Replacing $origPort with $newPort..."
			sudo sed -i -e "s/$origPort/$newPort/g" ${currConf} || error_exit $? "Failed on: sudo sed -i -e $origPort/$newPort"	
			documentRoot=`sudo grep -iE "^DocumentRoot" ${currConf} | awk '{print $2}' |  sed -e "s/\"//g"`
			
						
			rewriteModule="LoadModule rewrite_module modules\/mod_rewrite\.so"	
			sudo sed -i -e "s/\#$rewriteModule/$rewriteModule/g" ${currConf} || error_exit $? "Failed on: sudo sed -i -e s/\#$rewriteModule/$rewriteModule/g in ${currConf}"

			allowNone="AllowOverride None"			
			allowOverrideAll="AllowOverride All"
			# Replace the 2nd occurrence of "AllowOverride None" ( The 2nd line it appears in )
			secondLine=`grep -n "$allowNone" ${currConf} | head -2 | tail -1 | awk -F: '{print $1}'`
			sudo sed -i -e "${secondLine} s/$allowNone/$allowOverrideAll/" ${currConf} || error_exit $? "Failed on: sudo sed -i -e ${secondLine} s/$allowNone/$allowOverrideAll/2 in ${currConf}"	

			sudo chmod -R 777 *
			
			if  [ "${needPhp}" == "true" ] ; then
				sudo echo "<?php phpinfo(); ?>" >> $documentRoot/index.php
			fi

			if  [ "${applicationZipUrl}" != "NOT_REQUIRED" ] ; then
				mkdir tmpZipFolder
				cd tmpZipFolder
				echo "wgetting ${applicationZipUrl}"
				wget $applicationZipUrl
				ls *.zip | xargs echo "Unzipping"
				if  [ "${zipContentLevel}" == "0" ] ; then
					zipLocation=`pwd`
					cd $documentRoot
					rm -rf index.php
					echo "Unzipping to ${documentRoot} ... " 
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
					echo "Copying application files to ${documentRoot}/ ..."
					ls | xargs -I file sudo cp -r file $documentRoot/  
					cd ..
					rm -rf tmpZipFolder
				else
					echo "The content of ${applicationZipUrl} is the root folder of the zip file"
				fi
			else
				sudo cp -f index.html $documentRoot/
			fi							
			echo "End of $0" 
			exit 0
		fi	
    fi	
done

echo "End of $0" 