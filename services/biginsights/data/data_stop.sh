#! /bin/bash

echo "data_stop.sh: BigInsights is about to be stopped!!!!!"
rm -Rf /BI/hadoop
rm -Rf /BI/
userdel biadmin
sed -i '/biginsights/d' ~/.bashrc

echo "data_stop.sh: BigInsights is cleaned!!!!!"
