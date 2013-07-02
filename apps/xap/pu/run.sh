#!/bin/sh

LOCATORS=$1
GROUP=$2
GSHOME=$3
GSC_COUNT=$4
GSC_MEMORY=$5
JAVA_OPTIONS=$6

#sudo ulimit -n 32000
sudo sed -i '$ a\* soft nofile unlimited' /etc/security/limits.conf
#sudo sed -i '$ a\* hard nofile unlimited' /etc/security/limits.conf

sed -i "1i export JAVA_HOME=$GSHOME/../java" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh" 
sed -i "1i export NIC_ADDR=\"#eth0:ip#\"" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export GSA_JAVA_OPTIONS=\"-Xmx512m -Xms512m -Dcom.gs.agent.auto-shutdown-enabled=true -Dcom.gs.multicast.enabled=false \"" $GSHOME/bin/gs-agent.sh || error_exit $? "Failed updating gs-agent.sh"
sed -i "1i export GSC_JAVA_OPTIONS=\"-Xmx$GSC_MEMORY -Xms$GSC_MEMORY $JAVA_OPTIONS \"" $GSHOME/bin/gsc.sh || error_exit $? "Failed updating gsc.sh"
sed -i "1i export EXT_JAVA_OPTIONS=\"-Xmx$GSC_MEMORY -Xms$GSC_MEMORY -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4242 -Dcom.gs.multicast.discoveryPort=4242 -Dcom.gs.multicast.enabled=false  -Dcom.gs.jini_lus.groups=$GROUP -Dcom.gs.jini_lus.locators=$LOCATORS\"" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUPGROUPS=$GROUP" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=$GROUP" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUPLOCATORS=$LOCATORS" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export LOOKUP_LOCATORS_PROP=-Dcom.gs.jini_lus.locators=$LOCATORS" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"
sed -i "1i export PATH=$JAVA_HOME/bin:$PATH" $GSHOME/bin/setenv.sh || error_exit $? "Failed updating setenv.sh"

sed s/"\${JAVA_OPTIONS} -DagentId=\${AGENT_ID}"/"\${JAVA_OPTIONS} -DagentId=\${AGENT_ID} -Xloggc:\/tmp\/\$\$_gc.log -XX:+PrintClassHistogramAfterFullGC -XX:+PrintClassHistogramBeforeFullGC -XX:OnOutOfMemoryError=\"jstack -l -F  %p \> \/tmp\/\%p.thread.dump\.\`date +\%y_\%m_\%d.\%H_\%M_\%S\`\.txt\""/g $GSHOME/bin/gs.sh > $GSHOME/bin/gs.sh.backup2

cp $GSHOME/bin/gs.sh $GSHOME/bin/gs.sh.backup
cp $GSHOME/bin/gs.sh.backup2 $GSHOME/bin/gs.sh
rm $GSHOME/bin/gs.sh.backup2
chmod 755 $GSHOME/bin/gs.sh 

nohup sh $GSHOME/bin/gs-agent.sh gsa.global.lus=0 gsa.lus=0 gsa.global.gsm=0 gsa.gsm=0 gsa.gsc=$GSC_COUNT