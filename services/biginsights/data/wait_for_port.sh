#! /bin/bash
while [ -f `dirname $0`/installationRunning ] ;
do
        echo "data node: Waiting for the installation to complete"
    	sleep 30
done
while netstat -lnt | awk '$4 ~ /:50010$/ {exit 1}'; do sleep 10; done
echo " $port port is open for traffic"
while ! netstat -lnt | awk '$4 ~ /:50010$/ {exit 1}'; do sleep 10; done
echo "port is down for traffic"
exit 0


