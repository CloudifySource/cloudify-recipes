#! /bin/bash

if [[ $EUID -ne 0 ]]; then
	echo "Not root, need sudo"		
	if [ ! -d ~/.ssh ]; then
		sudo mkdir "~/.ssh"
	fi		
	sudo cp ./id_rsa ~root/.ssh/id_rsa
	sudo sudo chmod 600 ~root/.ssh/id_rsa
	echo "StrictHostKeyChecking no" | sudo tee ~root/.ssh/config
	echo "CheckHostIP no" | sudo tee -a ~root/.ssh/config
	echo "PasswordAuthentication no" | sudo tee -a ~root/.ssh/config
	sudo chmod 600 ~root/.ssh/config
	sudo ssh-keygen -y -f ~root/.ssh/id_rsa | sudo tee ~root/.ssh/id_rsa.pub
	sudo cat ~root/.ssh/id_rsa.pub | sudo tee -a ~root/.ssh/authorized_keys
	echo "sudo cp -R ~root/.ssh ~biadmin/"
	sudo cp -R ~root/.ssh ~biadmin/
	sudo chown -R biadmin.biadmin ~biadmin/.ssh
	sudo ls -al ~biadmin/.ssh
#	sudo cp ~biadmin/.ssh/authorized_keys ~biadmin/.ssh/id_rsa.pub

	public_ip=`curl http://169.254.169.254/latest/meta-data/public-ipv4`
	private_ip=`curl http://169.254.169.254/latest/meta-data/local-ipv4`

	echo "$public_ip master.$DOMAIN master" | sudo tee /etc/hosts
	echo "$private_ip master-internal.$DOMAIN master-internal" | sudo tee /etc/hosts
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		data_node_public_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/public-ipv4"`
		data_node_private_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/local-ipv4"`
		echo "$data_node_public_ip data-1.$2 data-1"| sudo tee -a /etc/hosts
		echo "$data_node_private_ip data-1-internal.$2 data-1-internal" | sudo tee -a /etc/hosts
	done
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		scp /etc/hosts root@$line:/etc/hosts
	done

else
	if [ ! -d ~/.ssh ]; then
		mkdir "~/.ssh"
	fi	
	cp ./id_rsa ~root/.ssh/id_rsa
	chmod 600 ~root/.ssh/id_rsa
	echo "StrictHostKeyChecking no" > ~root/.ssh/config
	echo "CheckHostIP no" >> ~root/.ssh/config
	echo "PasswordAuthentication no" >> ~root/.ssh/config
	chmod 600 ~root/.ssh/config
	ssh-keygen -y -f ~root/.ssh/id_rsa > ~root/.ssh/id_rsa.pub
	cat ~root/.ssh/id_rsa.pub >> ~root/.ssh/authorized_keys
	cp -R ~root/.ssh ~biadmin/
	chown -R biadmin.biadmin ~biadmin/.ssh
#	cp ~biadmin/.ssh/authorized_keys ~biadmin/.ssh/id_rsa.pub

	public_ip=`curl http://169.254.169.254/latest/meta-data/public-ipv4`
	private_ip=`curl http://169.254.169.254/latest/meta-data/local-ipv4`

	echo "$public_ip master.$DOMAIN master" >> /etc/hosts
	echo "$private_ip master-internal.$DOMAIN master-internal" >> /etc/hosts
	
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		data_node_public_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/public-ipv4"`
		data_node_private_ip=`ssh $line "curl http://169.254.169.254/latest/meta-data/local-ipv4"`
		echo "$data_node_public_ip data-1.$2 data-1" >> /etc/hosts
		echo "$data_node_private_ip data-1-internal.$2 data-1-internal" >> /etc/hosts
	done
	cat $1 | while read line
	do
		[ -z "$line" ] && continue
		scp /etc/hosts root@$line:/etc/hosts
		
	done

		
fi
