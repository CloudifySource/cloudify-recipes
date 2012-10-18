#BIGINSIGHTS_HOME=/opt/ibm/biginsights
#BIGINSIGHTS_LIB=/opt/ibm/biginsights/lib
#BIGINSIGHTS_VAR=/var/ibm/biginsights
#JAVA_HOME=/opt/ibm/biginsights/jdk

# while [ -f `dirname $0`/installationRunning ] ;
# do
# 	echo "rebalance.sh: Waiting for the master installation to complete"
#     sleep 30
# done

echo rebalance.sh sudo -i -u biadmin $BIGINSIGHTS_HOME/IHC/bin/start-balancer.sh  
sudo -i -u biadmin $BIGINSIGHTS_HOME/IHC/bin/start-balancer.sh 
echo rebalance.sh completed