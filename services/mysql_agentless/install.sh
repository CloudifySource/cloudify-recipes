#! /bin/bash

sudo debconf-set-selections <<< 'mysql-server-5.1 mysql-server/root_password password 123456'
sudo debconf-set-selections <<< 'mysql-server-5.1 mysql-server/root_password_again password 123456'
sudo apt-get -y install mysql-server
sudo sed -i 's/^bind-address.*=.*/bind-address = 0.0.0.0/g' /etc/mysql/my.cnf
sudo /etc/init.d/mysql restart
