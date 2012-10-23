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
	ps -ef | grep -iE "httpd" | grep -vi grep
	if [ $? -eq 0 ] ; then 
		ps -ef | grep -iE "httpd" | grep -vi grep | awk '{print $2}' | xargs sudo kill -9
	fi  
}

export PATH=$PATH:/usr/sbin:/sbin || error_exit $? "Failed on: export PATH=$PATH:/usr/sbin:/sbin"

# The existence of the usingYum file in the ext folder will later serve as a flag that "we" are on Red Hat or CentOS or Fedora or Amazon
echo "Using yum. Updating yum on one of the following : Red Hat, CentOS, Fedora, Amazon. " > usingYum
sudo yum -y -q update || error_exit $? "Failed on: sudo yum -y -q update"

sudo yum -y -q install unzip

killApacheProcess

if  [ "${needPhp}" == "true" ] ; then
  # Removing previous php installation if exists
  sudo yum remove -y -q php5* php*
  sudo rm -rf  /etc/php* || error_exit $? "Failed on: sudo rm -rf  /etc/php*"
  sudo rm -rf  /usr/bin/php* || error_exit $? "Failed on: sudo rm -rf  /usr/bin/php"
  sudo rm -rf  /usr/share/php* || error_exit $? "Failed on: sudo rm -rf /usr/share/php"  
fi  

# Removing previous httpd installation if exists
sudo yum remove -y -q httpd || error_exit $? "Failed on: sudo yum remove -y -q httpd"

# The following two statements are used since in some cases, there are leftovers after uninstall
sudo rm -rf /etc/httpd || error_exit $? "Failed on: sudo rm -rf /etc/httpd"
sudo rm -rf /usr/sbin/httpd || error_exit $? "Failed on: sudo rm -rf /usr/sbin/httpd"

echo "Using yum. Installing httpd on one of the following : Red Hat, CentOS, Fedora, Amazon"
sudo yum install -y -q httpd || error_exit $? "Failed on: sudo yum install -y -q httpd"

killApacheProcess

if  [ "${needPhp}" == "true" ] ; then

  needPhpdb=""
  if  [ "${dbType}" == "mysql" ] ; then
	  needPhpdb="php-mysql"
  else    
	echo "You need to implement code for another database (e.g. : for postgres)"
  fi

  sudo yum -y -q install php php-common php-pear php-pdo $needPhpdb php-gd php-mbstring php-mcrypt php-xml php-xmlrpc php-dom php-mhash
fi  

killApacheProcess

docParent="/var/www"
docRoot=$docParent/html

sudo chmod -R 777 $docParent

 
 
echo "End of $0" 