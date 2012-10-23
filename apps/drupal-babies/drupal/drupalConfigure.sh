#!/bin/bash

# args:
# $1 documentRoot
# $2 newMySqlConnString
# 

documentRoot=$1
drupalVersion=$2
databaseName=$3
dbUsername=$4
dbPassword=$5
dbPort=$6
dbHost=$7



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

sitesFolder="${documentRoot}/sites"
drupalDefaultFolder="${sitesFolder}/default"

drupalDefaultSettingsFilePath="${drupalDefaultFolder}/default.settings.php"
drupalSettingsFilePath="${drupalDefaultFolder}/settings.php"

echo "Chmodding a+w ${drupalDefaultFolder} ..."
sudo chmod -R 777 $sitesFolder

echo "Copying ${drupalDefaultSettingsFilePath} to ${drupalSettingsFilePath} ..."
sudo cp -f $drupalDefaultSettingsFilePath $drupalSettingsFilePath

echo "Chmodding a+w ${drupalSettingsFilePath} ..."
sudo chmod 777 $drupalSettingsFilePath
		
if  [ "${drupalVersion}" == "6" ] ; then
	echo "Setting db for Drupal ${drupalVersion} ..."
	origDbConnString="\$db_url"	
	newMySqlConnString="'mysql://${dbUsername}:${dbPassword}@${dbHost}/${databaseName}';"
	newMySqlConnString="\$db_url = ${newMySqlConnString}"
	echo "Replacing ${origDbConnString} with ${newMySqlConnString} #${origDbConnString} in ${drupalSettingsFilePath} ..."
	sudo sed -i -e "s%$origDbConnString%$newMySqlConnString\n#$origDbConnString%g" ${drupalSettingsFilePath}  || error_exit $? "Failed on: sudo sed -i -e s/\$origDbConnString ... in ${drupalSettingsFilePath}"
else
	echo "Setting db for Drupal ${drupalVersion} ..."
	origDbConnString="\$databases = array()"
	newMySqlConnString="\$databases = array();\n"
	newMySqlConnString="${newMySqlConnString} \n\$databases['default']['default'] = array("
	newMySqlConnString="${newMySqlConnString} \n      'driver' => 'mysql',"
	newMySqlConnString="${newMySqlConnString} \n      'database' => '${databaseName}',"
	newMySqlConnString="${newMySqlConnString} \n      'username' => '${dbUsername}',"
	newMySqlConnString="${newMySqlConnString} \n      'password' => '${dbPassword}',"
	newMySqlConnString="${newMySqlConnString} \n      'port' => ${dbPort},"
	newMySqlConnString="${newMySqlConnString} \n      'host' => '${dbHost}',"
	newMySqlConnString="${newMySqlConnString} \n      'prefix' => '',"
	newMySqlConnString="${newMySqlConnString} \n)"
	sudo sed -i -e "s%$origDbConnString%$newMySqlConnString%g" ${drupalSettingsFilePath} || error_exit $? "Failed on: sudo sed -i -e s/\$origDbConnString ... in ${drupalSettingsFilePath}"
fi 
	

sitesAll=${sitesFolder}/all
modules=$sitesAll/modules
themes=$sitesAll/themes
libraries=$sitesAll/libraries
 
echo "Creating ${modules} ...
sudo mkdir -p $modules

echo "Creating ${themes} ...
sudo mkdir -p $themes

echo "Creating ${libraries} ...
sudo mkdir -p $libraries
	
echo "Chmodding +w ${modules} ...	
sudo chmod a+w $modules

echo "Chmodding +w ${themes} ...	
sudo chmod a+w $themes

echo "Chmodding +w ${libraries} ...	
sudo chmod a+w $libraries
				
echo "End of $0" 