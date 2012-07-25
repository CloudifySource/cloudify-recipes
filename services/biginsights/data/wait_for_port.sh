#! /bin/bash
while netstat -lnt | awk '$4 ~ /:50010$/ {exit 1}'; do sleep 10; done
echo " $port port is open for traffic"
while ! netstat -lnt | awk '$4 ~ /:50010$/ {exit 1}'; do sleep 10; done
echo "port is down for traffic"
exit 0


