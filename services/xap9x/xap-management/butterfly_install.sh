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
sudo easy_install virtualenv || error_exit $? "Failed on: easy_install virtualenv"
virtualenv /tmp/virtenv_xapman --no-site-packages || error_exit $? "Failed on: virtualenv virtenv"
source "/tmp/virtenv_xapman/bin/activate" || error_exit $? "Failed on: source virtenv/bin/activate"
sudo yum -y install git python-pip gcc python-devel openssl-devel || error_exit $? "Failed to install requirements (git python-pip gcc python-devel)"
pip install pyOpenSSL==0.12 || error_exit $? "Failed to install pyOpenSSL v0.12"
git clone https://github.com/yohanakh/butterfly.git || error_exit $? "Failed to clone butterfly repository"
cd butterfly/ || error_exit $? "Failed to cd butterfly"
python setup.py install || error_exit $? "Failed on: python setup.py install"
deactivate
