#! /bin/bash

if [[ $EUID -ne 0 ]]; then
	echo "Not root, need sudo"		
	if [ ! -d ~root/.ssh ]; then
		sudo mkdir ~root/.ssh
	fi		
	sudo /etc/init.d/iptables stop
	sudo sed -i 's/^PermitRootLogin.*no/PermitRootLogin yes/g' /etc/ssh/sshd_config
	sudo cp ./id_rsa ~root/.ssh/id_rsa
	sudo sudo chmod 600 ~root/.ssh/id_rsa
	echo "StrictHostKeyChecking no" | sudo tee ~root/.ssh/config
	echo "CheckHostIP no" | sudo tee -a ~root/.ssh/config
	echo "PasswordAuthentication no" | sudo tee -a ~root/.ssh/config
	sudo chmod 600 ~root/.ssh/config
	sudo ulimit -n 16384
	echo "root hard nofile 16384" | sudo tee -a /etc/security/limits.conf
	echo "root soft nofile 16384" | sudo tee -a /etc/security/limits.conf
	sudo sed -i 's/^Defaults.*requiretty/#&/g' /etc/sudoers
	sudo sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
	sudo setenforce 0
	sudo groupadd biadmin
	sudo useradd -g biadmin -d /home/biadmin biadmin
	sudo echo $1 | passwd --stdin biadmin
	echo 'biadmin ALL=(ALL) NOPASSWD:ALL' | sudo tee -a /etc/sudoers
	ssh-keygen -y -f ~root/.ssh/id_rsa | sudo tee ~root/.ssh/id_rsa.pub
	cat ~root/.ssh/id_rsa.pub | sudo tee -a ~root/.ssh/authorized_keys
	sudo cp -R ~root/.ssh ~biadmin/
	sudo chown -R biadmin.biadmin ~biadmin/.ssh
#	cp ~biadmin/.ssh/authorized_keys ~biadmin/.ssh/id_rsa.pub

#	sudo mount /dev/xvdj /mnt
	echo "About to create folder " $2
	sudo mkdir $2
	sudo chown -R biadmin.biadmin $2
	sudo mkdir /mnt/hadoop && sudo ln -s /mnt/hadoop $2/hadoop
	sudo mkdir /mnt/ibm && sudo mkdir $2/var && sudo ln -s /mnt/ibm $2/var/ibm

else
	if [ ! -d ~/.ssh ]; then
		mkdir ~/.ssh
	fi	
	/etc/init.d/iptables stop
	sed -i 's/^PermitRootLogin.*no/PermitRootLogin yes/g' /etc/ssh/sshd_config
	echo about to create passwordless SSH access
	cp ./id_rsa ~root/.ssh/id_rsa
	chmod 600 ~root/.ssh/id_rsa
	echo "StrictHostKeyChecking no" > ~root/.ssh/config
	echo "CheckHostIP no" >> ~root/.ssh/config
	echo "PasswordAuthentication no" >> ~root/.ssh/config
	chmod 600 ~root/.ssh/config
	ulimit -n 16384
	echo "root hard nofile 16384" >> /etc/security/limits.conf
	echo "root soft nofile 16384" >> /etc/security/limits.conf
	sed -i 's/^Defaults.*requiretty/#&/g' /etc/sudoers
	sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
	setenforce 0
	groupadd biadmin
	useradd -g biadmin -d /home/biadmin biadmin
	echo $1 | passwd --stdin biadmin
	echo 'biadmin ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
	ssh-keygen -y -f ~root/.ssh/id_rsa > ~root/.ssh/id_rsa.pub
	cat ~root/.ssh/id_rsa.pub >> ~root/.ssh/authorized_keys
	cp -R ~root/.ssh ~biadmin/
	chown -R biadmin.biadmin ~biadmin/.ssh

	echo "About to create folder " $2
	mkdir $2
	chown -R biadmin.biadmin $2
	mkdir /mnt/hadoop && ln -s /mnt/hadoop $2/hadoop
	mkdir /mnt/ibm && mkdir $2/var && ln -s /mnt/ibm $2/var/ibm
fi
