#!/bin/sh -x

#sudo yum list -q installed glibc.i686
#if $? ge 0 then  : 

echo "sudo yum install -y -q glibc.i686"
sudo yum install -y -q glibc.i686


#sudo yum list -q installed libgcc_s.so.1
#if $? ge 0 then  : 
echo "yum install -y -q libgcc_s.so.1"
sudo yum install -y -q libgcc_s.so.1