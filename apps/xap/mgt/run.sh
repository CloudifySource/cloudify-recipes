#!/bin/sh -x

LOCATORS=$1
GROUP=$2
GSHOME=$3
LUS_MEMORY=$4
GSM_MEMORY=$5
JAVA_OPTIONS=$6

#sudo ulimit -n 32000
sudo sed -i '$ a\* soft nofile unlimited' /etc/security/limits.conf
#sudo sed -i '$ a\* hard nofile unlimited' /etc/security/limits.conf

sed -i "1i export JAVA_HOME=$GSHOME/../java" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export NIC_ADDR=\"#eth0:ip#\"" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export GSA_JAVA_OPTIONS=\"-Xmx512m -Dcom.gs.agent.auto-shutdown-enabled=true -Dcom.gs.multicast.enabled=false\"" $GSHOME/bin/gs-agent.sh || error_exit $? "Failed updating gs-agent.sh"
sed -i "1i export GSM_JAVA_OPTIONS=\"-Xmx${GSM_MEMORY} -Xms${GSM_MEMORY} -Dcom.gs.grid.gsm.pending-dispatch-delay=5000 -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4242 -Dgsm.excludeGscOnFailedInstance.disabled=true -Dcom.gs.multicast.enabled=false -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Xmn100m -XX:ParallelGCThreads=4 -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=/tmp/oom.hprof -Dcom.gs.transport_protocol.lrmi.connect_timeout=30000\"" $GSHOME/bin/gsm.sh || error_exit $? "Failed updating gsm.sh"
sed -i "1i export LUS_JAVA_OPTIONS=\"-Xmx${LUS_MEMORY} -Xms${LUS_MEMORY} -Dcom.gs.multicast.enabled=false -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4242  -Dcom.gigaspaces.lrmi.watchdog.level=FINE -Dcom.gs.multicast.discoveryPort=4242 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Xmn100m -XX:ParallelGCThreads=4 -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=/tmp/oom.hprof -Dcom.gs.transport_protocol.lrmi.connect_timeout=30000\"" $GSHOME/bin/startJiniLUS.sh || error_exit $? "Failed updating startJiniLUS.sh"
sed -i "1i export EXT_JAVA_OPTIONS=\"-Xmx${GSM_MEMORY} -Xms${GSM_MEMORY} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4242 -Dcom.gs.multicast.discoveryPort=4242 -Dcom.gs.multicast.enabled=false  -Dcom.gs.jini_lus.groups=$GROUP -Dcom.gs.jini_lus.locators=$LOCATORS\"" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUPGROUPS=$GROUP" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=$GROUP" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUPLOCATORS=$LOCATORS" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUP_LOCATORS_PROP=-Dcom.gs.jini_lus.locators=$LOCATORS" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export PATH=$JAVA_HOME/bin:$PATH" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"

sed s/"\${JAVA_OPTIONS} -DagentId=\${AGENT_ID}"/"\${JAVA_OPTIONS} -Dcom.gigaspaces.tools.excel.verbose=true -DagentId=\${AGENT_ID} -Xloggc:\/tmp\/\$\$_gc.log -XX:+PrintClassHistogramAfterFullGC -XX:+PrintClassHistogramBeforeFullGC -XX:OnOutOfMemoryError=\"jstack -l -F  %p \> \/tmp\/\%p.thread.dump\.\`date +\%y_\%m_\%d.\%H_\%M_\%S\`\.txt\""/g $GSHOME/bin/gs.sh > $GSHOME/bin/gs.sh.backup2

cp $GSHOME/bin/gs.sh $GSHOME/bin/gs.sh.backup
cp $GSHOME/bin/gs.sh.backup2 $GSHOME/bin/gs.sh
rm $GSHOME/bin/gs.sh.backup2
chmod 755 $GSHOME/bin/gs.sh 

nohup sh $GSHOME/bin/gs-agent.sh gsa.global.lus=0 gsa.lus=1 gsa.global.gsm=0 gsa.gsm=1 gsa.gsc=0