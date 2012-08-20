#! /bin/bash

bucket="yoram-biginsights"
#ATTACHMENT_VERSION="v1.3.1_64bit"

#folder="biginsights_basic/$ATTACHMENT_VERSION"
if [ -f "/tmp/bi_download.lock" ]
then
  echo "Not first boot - skipping attachment download"
else
	wget -nv https://s3.amazonaws.com/yoram-biginsights/iib14_linux_64.tar.gz
 	tar --index-file /tmp/biginsights.tar.log -xvvf iib*_linux_64.tar.gz -C /tmp/
  	rm iib*_linux_64.tar.gz  
  	touch /tmp/bi_download.lock
fi

#sed -i 's/^Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers

if [[ $EUID -ne 0 ]]; then		
	echo "Not root, need sudo"		
#	sudo mkdir /mnt/hadoop
#	sudo ln -s /mnt/hadoop /hadoop 
#	sudo mkdir /mnt/ibm
#	sudo ln -s /mnt/ibm /var/ibm	
	sudo groupadd biadmin
	sudo useradd -g biadmin -d /home/biadmin biadmin
	sudo echo $1 | passwd --stdin biadmin
	sudo echo $1 | passwd --stdin root
	sudo yum -y -q install expect
else
	groupadd biadmin
	useradd -g biadmin -d /home/biadmin biadmin
	echo $1 | sudo passwd --stdin biadmin
	echo $1 | sudo passwd --stdin root
	yum -y -q install expect
fi


exit 0


