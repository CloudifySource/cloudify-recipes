#! /bin/bash

echo "data_stop.sh: BigInsights is about to be stopped!!!!!"
def config = new ConfigSlurper().parse(new File("master-service.properties").toURL())
rm -Rf /hadoop
rm -Rf config.ibmHome
userdel biadmin
sed -i '/biginsights/d' ~/.bashrc

echo "data_stop.sh: BigInsights is cleaned!!!!!"
