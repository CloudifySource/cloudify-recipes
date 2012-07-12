#! /bin/bash

#bucket="ibm.rightscale.us"
bucket="yoram-biginsights"
#ATTACHMENT_VERSION="v1.3.1_64bit"

#folder="biginsights_basic/$ATTACHMENT_VERSION"
if [ -f "/tmp/bi_download.lock" ]
then
  echo "Not first boot - skipping attachment download"
else
	if [ -f /usr/bin/s3cmd ]
	then
		echo s3cmd is already available
	else
		wget http://s3tools.org/repo/RHEL_6/s3tools.repo
		if [[ $EUID -ne 0 ]]; then
			echo "Not root, need sudo"		
			sudo mv s3tools.repo /etc/yum.repos.d
			sudo yum -y -q install s3cmd
		else
			mv s3tools.repo /etc/yum.repos.d
			yum -y -q install s3cmd
		fi
		
		pwd
		mv .s3cfg ~/
	fi
#  s3cmd get s3://$bucket/$folder/* /tmp
   s3cmd get s3://yoram-biginsights/iib14_linux_64.tar.gz /tmp/iib14_linux_64.tar.gz
 tar --index-file /tmp/biginsights.tar.log -xvvf /tmp/iib*_linux_64.tar.gz -C /tmp/
  rm /tmp/iib*_linux_64.tar.gz  
  touch /tmp/bi_download.lock
fi

#sed -i 's/^Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers

if [[ $EUID -ne 0 ]]; then		
	echo "Not root, need sudo"		
#	sudo mkdir /mnt/hadoop
#	sudo ln -s /mnt/hadoop /hadoop 
#	sudo mkdir /mnt/ibm
#	sudo ln -s /mnt/ibm /var/ibm	
	sudo mkdir /hadoop
	sudo mkdir /var/ibm
	sudo groupadd biadmin
	sudo useradd -g biadmin -d /home/biadmin biadmin
	echo $1 | passwd --stdin biadmin
	echo $1 | passwd --stdin root
	sudo yum -y -q install expect
else
	mkdir /hadoop
	mkdir /var/ibm
	groupadd biadmin
	useradd -g biadmin -d /home/biadmin biadmin
	echo $1 | sudo passwd --stdin biadmin
	echo $1 | sudo passwd --stdin root
	yum -y -q install expect
fi


exit 0


