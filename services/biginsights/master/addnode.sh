echo addnode.sh adding node $1 role $2
#BIGINSIGHTS_HOME=/opt/ibm/biginsights
#BIGINSIGHTS_LIB=/opt/ibm/biginsights/lib
#BIGINSIGHTS_VAR=/var/ibm/biginsights
#JAVA_HOME=/opt/ibm/biginsights/jdk

scp /home/biadmin/.ssh $1:/home/biadmin/
ssh $1 chown -R biadmin /home/biadmin/.ssh
ssh $1 chown /etc/init.d/sshd restart

while [ -f `dirname $0`/installationRunning ] ;
do
	echo "addNode.sh: Waiting for the master installation to complete"
    sleep 30
done

echo addnode.sh sudo -i -u biadmin /opt/ibm/biginsights/bin/addnode.sh $2 $1 >> /tmp/addnode.log
sudo -i -u biadmin $BIGINSIGHTS_HOME/bin/addnode.sh $2 $1 >> /tmp/addnode.log
echo addnode.sh completed