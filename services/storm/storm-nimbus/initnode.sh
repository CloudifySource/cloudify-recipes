#!/bin/bash

sudo yum -y install gcc-c++ autoconf automake make libtool libuuid-devel git

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
git clone https://github.com/nathanmarz/jzmq.git
cd jzmq
./autogen.sh
./configure
make
sudo make install

