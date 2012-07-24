#! /bin/bash
while netstat -lnt | awk '$4 ~ /:60000$/ {exit 1}'; do sleep 10; done
echo " $port port is open for traffic"
while ! netstat -lnt | awk '$4 ~ /:60000$/ {exit 1}'; do sleep 10; done
echo "port is down for traffic"
exit 0


