#!/bin/bash -x

# args:
# $1 A command separated list of my.cnf section names
# $2 A command separated list of my.cnf variable names
# $3 A command separated list of my.cnf values for the above variable names
# 

sectionNames=$1
variableNames=$2
newValues=$3

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

function killMySqlProcess {
	sudo service mysql stop
	ps -ef | grep -iE "mysqld" | grep -ivE "gigaspaces|GSC|GSA|grep"
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -iE "mysqld" | grep -ivE "gigaspaces|GSC|GSA|grep" | awk '{print $2}' | xargs sudo kill -9
	fi  
}

export PATH=$PATH:/usr/sbin:/sbin:/usr/bin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin"

if sudo grep -q -E '[^!]requiretty' /etc/sudoers; then
    echo "Defaults:`whoami` !requiretty" | sudo tee /etc/sudoers.d/`whoami` >/dev/null
    sudo chmod 0440 /etc/sudoers.d/`whoami`
fi

# The existence of the usingAptGet file in the ext folder will later serve as a flag that "we" are on Ubuntu or Debian or Mint
echo "Using apt-get. Updating apt-get on one of the following : Ubuntu, Debian, Mint" > usingAptGet
sudo apt-get -y -q update || error_exit $? "Failed on: sudo apt-get -y update"

echo "#1 Killing old mysql process if exists..."
killMySqlProcess

# Removing previous mysql installation if exists
echo "Purging previous mysql installation if exists..."
sudo apt-get -y -q purge mysql-client* mysql-server* mysql-common*

# The following two statements are used since in some cases, there are leftovers after uninstall
echo "Removing old stuff if exists..."
sudo rm -rf /etc/mysql || error_exit $? "Failed on: sudo rm -rf /etc/mysql"


echo "Using apt-get. Updating apt-get on one of the following : Ubuntu, Debian, Mint" 
sudo DEBIAN_FRONTEND='noninteractive' apt-get -o Dpkg::Options::='--force-confnew' -q -y install mysql-server-core mysql-server mysql-client mysql-common || error_exit $? "Failed on: sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -q  mysql-server ... "

echo "Killing old mysql process if exists b4 ending the installation..."
killMySqlProcess

sectionNamesLen=`expr length "$sectionNames"`
if [ $sectionNamesLen -gt 0 ] ; then

	myCnfPath=`sudo find / -name "my.cnf"`
	if [ -f "${myCnfPath}" ] ; then

		sectionNames=$1
		variableNames=$2
		newValues=$3

		IFS=, read -a sectionNamesArr <<< "$sectionNames"
		IFS=, read -a variableNamesArr <<< "$variableNames"
		IFS=, read -a newValuesArr <<< "$newValues"
		echo "IFS is ${IFS}"
		echo "${sectionNamesArr[@]}"
		echo "${variableNamesArr[@]}"
		echo "${newValuesArr[@]}"

		variableCounter=${#variableNamesArr[@]}

		for (( i=0; i<${variableCounter}; i++ ));
		do
			currSection="\[${sectionNamesArr[$i]}\]"
			currVariable="${variableNamesArr[$i]}"
			currNewValue="${newValuesArr[$i]}"
			currNewLine="${currVariable}=${currNewValue}"
			echo "Commenting $currVariable in $myCnfPath ... "
			sudo sed -i -e "s/^$currVariable/#$currVariable/g" $myCnfPath
			
			jointStr="${currSection}\n${currNewLine}"
			echo "Setting ${currNewLine} in the ${currSection} section of $myCnfPath ... "
			sudo sed -i -e "s/$currSection/$jointStr/g" $myCnfPath		
		done
	fi
fi
echo "End of $0"