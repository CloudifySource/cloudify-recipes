#BIGINSIGHTS_HOME=/opt/ibm/biginsights
#BIGINSIGHTS_LIB=/opt/ibm/biginsights/lib
#BIGINSIGHTS_VAR=/var/ibm/biginsights
#JAVA_HOME=/opt/ibm/biginsights/jdk

#while [ -f `dirname $0`/installationRunning ] ;
#do
#	echo "dfs.sh: Waiting for the master installation to complete"
#    sleep 30
#done


echo hadoop.sh sudo -i -u biadmin /opt/ibm/biginsights/IHC/bin/hadoop $@  
sudo -i -u biadmin $BIGINSIGHTS_HOME/IHC/bin/hadoop $@ 
echo hadoop.sh completed