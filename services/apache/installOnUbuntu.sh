#!/bin/bash

# args:
# $1 need php or not ? "true" or "false" (string, not boolean)
# $2 dbType (mysql,postgres, etc.)

needPhp="$1"
dbType="$2"

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}


function killApacheProcess {
	ps -ef | grep -iE "apache2" | grep -vi grep
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -iE "apache2" | grep -vi grep | awk '{print $2}' | xargs sudo kill -9
	fi  
}


export PATH=$PATH:/usr/sbin:/sbin:/usr/bin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin"

# The existence of the usingAptGet file in the ext folder will later serve as a flag that "we" are on Ubuntu or Debian or Mint
echo "Using apt-get. Updating apt-get on one of the following : Ubuntu, Debian, Mint" > usingAptGet
sudo apt-get -y -q update || error_exit $? "Failed on: sudo apt-get -y update"

sudo apt-get -y -q install unzip

#sudo /etc/init.d/apache2 stop
# Just in case the above doesn't work
killApacheProcess

if  [ "${needPhp}" == "true" ] ; then
  sudo apt-get --purge -q -y remove php5* php* 
  sudo rm -rf  /etc/php* || error_exit $? "Failed on: sudo rm -rf  /etc/php*"
  sudo rm -rf  /usr/bin/php* || error_exit $? "Failed on: sudo rm -rf  /usr/bin/php"
  sudo rm -rf  /usr/share/php* || error_exit $? "Failed on: sudo rm -rf /usr/share/php"
fi  

sudo rm -rf /var/www/*

# Removing previous apache2 installation if exist
sudo apt-get -y -q purge apache2.2-common apache2* || error_exit $? "Failed on: sudo apt-get -y -q purge apache2*"

# The following statements are used since in some cases, there are leftovers after uninstall
sudo rm -rf /etc/apache2 || error_exit $? "Failed on: sudo rm -rf /etc/apache2"
sudo rm -rf /usr/sbin/apache2 || error_exit $? "Failed on: sudo rm -rf /usr/sbin/apache2"
sudo rm -rf /usr/lib/apache2 || error_exit $? "Failed on: sudo rm -rf /usr/lib/apache2"
sudo rm -rf /usr/share/apache2 || error_exit $? "Failed on: sudo rm -rf /usr/share/apache2"

echo "Using apt-get. Installing apache2 on one of the following : Ubuntu, Debian, Mint"
sudo apt-get install -y -q apache2 || error_exit $? "Failed on: sudo apt-get install -y -q apache2"

#sudo /etc/init.d/apache2 stop
# Just in case the above doesn't work
killApacheProcess

if  [ "${needPhp}" == "true" ] ; then

  needPhpdb=""
  if  [ "${dbType}" == "mysql" ] ; then
	needPhpdb="php5-mysql"
  else    
	echo "You need to implement code for another database (e.g. : for postgres)"
  fi

  sudo apt-get -y -q install php5 libapache2-mod-php5 php5-common php5-curl php5-cli php-pear $needPhpdb php5-gd php5-mcrypt php5-xmlrpc php5-sqlite php-xml-parser
fi 

#php-pdo
#php-mbstring 
#php-xml 
#php-dom 

killApacheProcess

docRoot="/var/www"

sudo chmod -R 777 $docRoot

echo "End of $0"

