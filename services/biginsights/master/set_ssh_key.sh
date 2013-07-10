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
	sudo ssh-keygen -y -f ~root/.ssh/id_rsa | sudo tee ~root/.ssh/id_rsa.pub
	sudo cat ~root/.ssh/id_rsa.pub | sudo tee -a ~root/.ssh/authorized_keys
	echo "sudo cp -R ~root/.ssh ~biadmin/"
	sudo cp -R ~root/.ssh ~biadmin/
	sudo chown -R biadmin.biadmin ~biadmin/.ssh
else
	if [ ! -d ~/.ssh ]; then
		mkdir ~/.ssh
	fi
    /etc/init.d/iptables stop
	sed -i 's/^PermitRootLogin.*no/PermitRootLogin yes/g' /etc/ssh/sshd_config
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
		
fi
