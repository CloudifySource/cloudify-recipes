#! /bin/bash


echo "master_stop.sh: BigInsights is about to be stopped!!!!!"

$1opt/ibm/biginisights/bin/stop-all.sh
	

rm -Rf $1*
rm -Rf /tmp/*

userdel biadmin
sed -i '/biginsights/d' ~/.bashrc

echo "master_stop.sh: BigInsights folders cleaned!!!!!"
