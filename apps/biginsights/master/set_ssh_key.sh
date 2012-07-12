#! /bin/bash

if [[ $EUID -ne 0 ]]; then
	echo "Not root, need sudo"		
	if [ ! -d ~/.ssh ]; then
		sudo mkdir "~/.ssh"
	fi		
	sudo cp $1 $2
	sudo chown biadmin $2
	sudo chmod 400 $2
	sudo sed -i 's/^PermitRootLogin forced-commands-only/PermitRootLogin without-password/g' /etc/ssh/sshd_config
	sudo sed -i 's/^command="echo ['\'']Please login as the ec2-user user rather than root user.['\''];echo;sleep 10" ssh-rsa/ssh-rsa/g' /root/.ssh/authorized_keys
	sudo /etc/init.d/sshd restart
else
	if [ ! -d ~/.ssh ]; then
		mkdir "~/.ssh"
	fi		
	cp $1 $2
	chown biadmin $2
	chmod 400 $2
	sed -i 's/^PermitRootLogin forced-commands-only/PermitRootLogin without-password/g' /etc/ssh/sshd_config
	sed -i 's/^command="echo ['\'']Please login as the ec2-user user rather than root user.['\''];echo;sleep 10" ssh-rsa/ssh-rsa/g' /root/.ssh/authorized_keys
	/etc/init.d/sshd restart
fi
