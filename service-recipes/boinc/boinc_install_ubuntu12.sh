#!/bin/bash

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
# 
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}

if [ ! -z `which boinc` ]; then
	exit 0
fi

#clean up if borken env.
sudo apt-get remove -y boinc-client
sudo rm -rf /var/lib/boinc-client
sudo rm -rf /etc/boinc-client


#workaround for ubuntu 12.04 repository lock
#see http://setiathome.berkeley.edu/forum_thread.php?id=67864&nowrap=true#1229806
sudo apt-get -y install python-software-properties
sudo add-apt-repository ppa:costamagnagianfranco/boinc

sudo apt-get -y update
sudo apt-get -y install boinc-client || error_exit $? "Failed installing boinc-client"

cp /etc/default/boinc-client conf_boic-client || error_exit $? "Failed copying boinc-client config"

sed -i "s/ENABLED=.*/ENABLED=\"0\"/g" conf_boic-client || error_exit $? "Failed modifying boinc-client config ENABLED property"

sed -i "s/SCHEDULE=.*/SCHEDULE=\"0\"/g" conf_boic-client || error_exit $? "Failed modifying boinc-client config SCHEDULE property"

sed -i "s/BOINC_USER=.*/BOINC_USER=\"ubuntu\"/g" conf_boic-client || error_exit $? "Failed modifying boinc-client config BOINC_USER property"

sudo mv -f conf_boic-client /etc/default/boinc-client || error_exit $? "Failed copying boinc-client config"

sudo cp cc_config.xml /etc/boinc-client/cc_config.xml