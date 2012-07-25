#!/bin/bash

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
sudo rm -f /home/`whoami`/{.,}*mysql* || error_exit $? "Failed on: sudo rm -f /home/`whoami`/{.,}*mysql*" 


echo "End of $0"
