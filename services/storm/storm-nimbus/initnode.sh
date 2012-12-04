#!/bin/bash

if [ -z "$JAVA_HOME" ]; then export JAVA_HOME=$HOME/java; fi

if [ -f /usr/local/lib/libzmq.a ]; then
  exit 0
fi

sudo yum -y install gcc-c++ autoconf automake make libtool libuuid-devel git
if [ -f "/etc/issue" -a -n "`grep -i centos /etc/issue`" ]; then
  sudo rpm -Uvh http://repo.webtatic.com/yum/centos/5/latest.rpm
  sudo yum -y install --enablerepo=webtatic git-all
  sudo yum -y install pkgconfig e2fsprogs-devel
fi

# zmq
wget "http://download.zeromq.org/zeromq-2.1.7.tar.gz"
tar xzf zeromq-2.1.7.tar.gz
cd zeromq-2.1.7
./autogen.sh
autoconf
automake
./configure
sudo make install
cd ..

# jzmq
env GIT_SSL_NO_VERIFY=true git clone https://github.com/nathanmarz/jzmq.git
cd jzmq
./autogen.sh
./configure
make
sudo make install

