#!/bin/bash

export EXT_JAVA_OPTIONS="-Dcom.gs.multicast.enabled=false -Dcom.gs.multicast.discoveryPort=4242 -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4242 -Dcom.gigaspaces.start.httpPort=4243 -Dcom.gigaspaces.system.registryPort=4244 -Dcom.gs.transport_protocol.lrmi.bind-port=4242-4342"
${XAP_BIN}/gs.sh



