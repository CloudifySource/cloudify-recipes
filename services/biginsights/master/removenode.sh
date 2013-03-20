echo removenode.sh adding node $1 role $2

while [ -f `dirname $0`/installationRunning ] ;
do
	echo "removeNode.sh: Waiting for the master installation to complete"
    sleep 30
done

echo removenode.sh sudo -i -u biadmin $BIGINSIGHTS_HOME/bin/removenode.sh $2 $1 >> /tmp/removenode.log
sudo -i -u biadmin $BIGINSIGHTS_HOME/bin/removenode.sh hadoop $2 $1 >> /tmp/removenode.log
echo removenode.sh completed