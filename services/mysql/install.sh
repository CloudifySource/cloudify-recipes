#!/bin/bash

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
	ps -ef | grep -iE "mysql" | grep -ivE "gigaspaces|GSC|GSA|grep"
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -iE "mysql" | grep -ivE "gigaspaces|GSC|GSA|grep" | awk '{print $2}' | xargs sudo kill -9
	fi  
}

export PATH=$PATH:/usr/sbin:/sbin:/usr/bin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin"

# The existence of the usingYum file in the ext folder will later serve as a flag that "we" are on Red Hat or CentOS or Fedora or Amazon
echo "Using yum. Updating yum on one of the following : Red Hat, CentOS, Fedora, Amazon. " > usingYum
sudo yum -y -q update || error_exit $? "Failed on: sudo yum -y -q update"

echo "#1 Killing old mysql process if exists..."
killMySqlProcess

echo "Removing previous mysql installation if exists..."
sudo yum -y -q remove mysql mysql-server

# The following two statements are used since in some cases, there are leftovers after uninstall
echo "Removing old stuff if exists..."
sudo rm -rf /usr/lib/mysql* || error_exit $? "Failed on: sudo rm -rf /usr/lib/mysql*"
sudo rm -rf /var/lib/mysql* || error_exit $? "Failed on: sudo rm -rf /var/lib/mysql*"
sudo rm -rf /usr/share/mysql* || error_exit $? "Failed on: sudo rm -rf /usr/sharemysql*"
sudo rm -rf /usr/bin/mysql* || error_exit $? "Failed on: sudo rm -rf /usr/bin/mysql*"
sudo rm -rf /var/run/mysql* || error_exit $? "Failed on: sudo rm -rf /var/run/mysql*"
sudo rm -rf /var/bin/mysql* || error_exit $? "Failed on: sudo rm -rf /var/bin/mysql*"
sudo rm -rf /etc/mysql* || error_exit $? "Failed on: sudo rm -rf /etc/mysql*"
sudo rm -rf /etc/rc.d/init.d/mysql* || error_exit $? "Failed on: sudo rm -rf /etc/rc.d/init.d/mysql*"
sudo rm -rf /usr/libexec/mysql* || error_exit $? "Failed on: sudo rm -rf /usr/libexec/mysqld*" 
sudo rm -rf /etc/my.cnf || error_exit $? "Failed on: sudo rm -rf /etc/my.cnf" 
sudo rm -rf /var/log/mysql* || error_exit $? "Failed on: sudo rm -rf /var/log/mysql*" 
#sudo rm -f /home/`whoami`/{.,}*mysql* || error_exit $? "Failed on: sudo rm -f /home/`whoami`/{.,}*mysql*" 

echo "Using yum. Installing mysql on one of the following : Red Hat, CentOS, Fedora, Amazon"

# This master-slave implementation requires mysql5.1+ version. 
# So if the available version is 5.0, the recipe retrievs mysql5.5 from the webtatic repo.
currMysqlVersion=`yum list mysql mysql-server | grep mysql | grep -cv "5.0"`
if [ $currMysqlVersion -eq 0 ] ; then
  echo "Getting rpm repo.webtatic.com ..." 
  sudo rpm --force -Uvh http://repo.webtatic.com/yum/centos/5/latest.rpm || error_exit $? "Failed on: sudo rpm -Uvh http://repo.webtatic.com/yum/centos/5/latest.rpm"
  echo "Installing libmysqlclient15 from webtatic repo ..." 
  sudo yum install -y -q libmysqlclient15 --enablerepo=webtatic || error_exit $? "Failed on: sudo yum install -y -q libmysqlclient15 --enablerepo=webtatic"
  echo "Installing mysql55 from webtatic repo ..." 
  sudo yum install -y -q mysql55 mysql55-server --enablerepo=webtatic || error_exit $? "Failed on: sudo yum install -y -q mysql55 mysql55-server --enablerepo=webtatic"
  echo "Reinstalling mysql55-libs from webtatic repo ..." 
  sudo yum reinstall -y -q mysql55-libs --enablerepo=webtatic || error_exit $? "Failed on: sudo reinstall -y -q mysql55-libs --enablerepo=webtatic"
else
  sudo yum install -y -q mysql mysql-server || error_exit $? "Failed on: sudo yum install -y -q mysql mysql-server "
  echo "Reinstalling mysql-libs ..."
  sudo yum reinstall -y -q mysql-libs || error_exit $? "Failed on: sudo yum install -y -q mysql mysql-server "
fi

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


