#!/bin/bash -x

SUDO=sudo
if [ $USER = root ]; then
	SUDO=""
fi

if [ -z "$JAVA_HOME" ]; then export JAVA_HOME=$HOME/java; fi

if [ -f /usr/local/lib/libzmq.a ]; then
  exit 0
fi

$SUDO yum -y install pkgconfig gcc-c++ glibc-headers autoconf.noarch automake make libtool libuuid-devel git
if [ -f "/etc/issue" -a -n "`grep -i centos /etc/issue`" ]; then
  $SUDO rpm -Uvh http://repo.webtatic.com/yum/centos/5/latest.rpm
  $SUDO yum -y install --enablerepo=webtatic git-all
  $SUDO yum -y install pkgconfig e2fsprogs-devel
fi

#libtool
LIBTOOLDIR=libtool-1.5.24
wget ftp://ftp.gnu.org/gnu/libtool/${LIBTOOLDIR}.tar.gz
tar xzf ${LIBTOOLDIR}.tar.gz
cd ${LIBTOOLDIR}
./configure
make
cd ..

# zmq
wget "http://download.zeromq.org/zeromq-2.1.7.tar.gz"
tar xzf zeromq-2.1.7.tar.gz
cd zeromq-2.1.7
./autogen.sh
autoconf
automake
./configure
cp -f ../${LIBTOOLDIR}/libtool .
$SUDO make install
cd ..

# jzmq
env GIT_SSL_NO_VERIFY=true git clone https://github.com/nathanmarz/jzmq.git
cd jzmq
./autogen.sh
./configure
make
$SUDO make install

