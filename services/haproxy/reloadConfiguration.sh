#!/bin/bash

# This script reloads haproxy configuration after change with an approach that has minimal service impact.

echo "Reload haproxy configuration ... "
  
if test -z "$1"; then
    configureFile="/etc/haproxy_rabbitmq.conf"
else
    configureFile="$1"
fi


if test -z "$2"; then
    pidFile="/var/run/haproxy.pid"
else
    pidFile="$2"
fi

haproxy -f ${configureFile} -p ${pidFile} -sf $(<${pidFile})

echo "Reloaded haproxy configuration. "