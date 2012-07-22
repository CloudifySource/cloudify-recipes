#! /bin/bash

if [[ $EUID -ne 0 ]]; then
	sudo groupadd biadmin
	sudo useradd -g biadmin -d /home/biadmin biadmin
	sudo echo 123456 | sudo passwd --stdin biadmin
	sudo echo 123456 | sudo passwd --stdin root
	sudo cp $1 $2
	sudo chown biadmin $2
	sudo chmod 400 $2
	sudo sed -i 's/^PermitRootLogin forced-commands-only/PermitRootLogin without-password/g' /etc/ssh/sshd_config
	sudo sed -i 's/^command="echo ['\'']Please login as the ec2-user user rather than root user.['\''];echo;sleep 10" ssh-rsa/ssh-rsa/g' /root/.ssh/authorized_keys
	sudo echo -e "\nHost *\n\t	UserKnownHostsFile=/dev/null\n\t		StrictHostKeyChecking no" >> /etc/ssh/ssh_config
	sudo /etc/init.d/sshd restart
else
	groupadd biadmin
	useradd -g biadmin -d /home/biadmin biadmin
	echo 123456 | sudo passwd --stdin biadmin
	echo 123456 | sudo passwd --stdin root
	cp $1 $2
	chown biadmin $2
	chmod 400 $2
	sed -i 's/^PermitRootLogin forced-commands-only/PermitRootLogin without-password/g' /etc/ssh/sshd_config
	sed -i 's/^command="echo ['\'']Please login as the ec2-user user rather than root user.['\''];echo;sleep 10" ssh-rsa/ssh-rsa/g' /root/.ssh/authorized_keys
	echo -e "\nHost *\n\t	UserKnownHostsFile=/dev/null\n\t		StrictHostKeyChecking no" >> /etc/ssh/ssh_config
	/etc/init.d/sshd restart
fi


