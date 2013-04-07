#!/bin/bash
#
# Installs the RabbitMQ server

echo "###### Installing Rabbit MQ Server"

# Install erlang
if rpm -qa | grep -q erlang; then
	echo "Erlang is already installed"
else

	if rpm -qa | grep -q epel-release; then
		echo "epel is already installed"
	else
		wget -O /tmp/epel-release-6-8.noarch.rpm http://mirror.bytemark.co.uk/fedora/epel/6/x86_64/epel-release-6-8.noarch.rpm
		rpm -i /tmp/epel-release-6-8.noarch.rpm
	fi
	
	yum -y install erlang
fi

# Install rabbit mq
if rpm -qa | grep -q rabbitmq-server; then
	echo "Rabbit MQ Server is already installed"
else
	wget rabbitmq-signing-key-public.asc http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
	rpm --import rabbitmq-signing-key-public.asc
	wget -O /tmp/rabbitmq-server-3.0.3-1.noarch.rpm http://www.rabbitmq.com/releases/rabbitmq-server/v3.0.3/rabbitmq-server-3.0.3-1.noarch.rpm
	yum -y install /tmp/rabbitmq-server-3.0.3-1.noarch.rpm
fi

# Adding RabbitMQ management 
rabbitmq-plugins enable rabbitmq_management

echo "##### Finished installing Rabbit MQ Server"

exit 0
