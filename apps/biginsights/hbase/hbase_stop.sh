#! /bin/bash

echo "hbase_stop.sh: BigInsights is about to be stopped!!!!!"

rm -Rf /hadoop
rm -Rf /var/ibm
rm -Rf /tmp/bi*
rm -Rf /tmp/.com_ibm_tools_attach/
rm -Rf /opt/ibm/
userdel biadmin
sed -i '/biginsights/d' ~/.bashrc

echo "hbase_stop.sh: BigInsights is cleaned!!!!!"
