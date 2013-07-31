export WEBUI_JAVA_OPTIONS="-Xmx2g -Xms1g -Dcom.gigaspaces.webui.common.level=FINE -Dcom.gigaspaces.webui.pu.events.level=FINE -Dcom.gigaspaces.webui.lifecycle.level=FINER -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=4242 -Dcom.gs.multicast.enabled=false -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Xmn100m -XX:ParallelGCThreads=4 -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=/tmp/oom.hprof -Dcom.gs.transport_protocol.lrmi.connect_timeout=30000"
#!/bin/bash
# 		WEBUI_JAVA_OPTIONS 	- Extended java options that are proprietary defined  for Web UI such as heap size, system properties or other JVM arguments that can be passed to the JVM command line. 
#							- These settings can be overridden externally to this script.

if [ "${JSHOMEDIR}" = "" ] ; then
  JSHOMEDIR=`dirname $0`/../../
fi
export JSHOMEDIR

# WEBUI_JAVA_OPTIONS=; export WEBUI_JAVA_OPTIONS
COMPONENT_JAVA_OPTIONS="${WEBUI_JAVA_OPTIONS}"
export COMPONENT_JAVA_OPTIONS

# The call to setenv.sh can be commented out if necessary.
. $JSHOMEDIR/bin/setenv.sh

LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=${LOOKUPGROUPS}; export LOOKUP_GROUPS_PROP

if [ "${LOOKUPLOCATORS}" = "" ] ; then
LOOKUPLOCATORS=; export LOOKUPLOCATORS
fi
LOOKUP_LOCATORS_PROP="-Dcom.gs.jini_lus.locators=${LOOKUPLOCATORS}"; export LOOKUP_LOCATORS_PROP

JETTY_JARS="${JSHOMEDIR}"/lib/platform/jetty/*
export JETTY_JARS

PLATFORM_CLASSPATH=${JETTY_JARS}:"${JSHOMEDIR}"/lib/platform/commons/commons-collections-3.2.jar
PLATFORM_CLASSPATH=${PLATFORM_CLASSPATH}:"${JSHOMEDIR}"/lib/platform/commons/commons-lang-2.3.jar




COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} $bootclasspath ${RMI_OPTIONS} ${LOOKUP_LOCATORS_PROP} ${LOOKUP_GROUPS_PROP} -Djava.security.policy=${POLICY} -Dcom.gigaspaces.logger.RollingFileHandler.time-rolling-policy=monthly -Dcom.gs.home=${JSHOMEDIR} -classpath "${PRE_CLASSPATH}${CPS}${GS_JARS}${CPS}${SPRING_JARS}${CPS}${EXT_JARS}${CPS}${PLATFORM_CLASSPATH}${CPS}${POST_CLASSPATH}" org.openspaces.launcher.Launcher"

# Extract the Web UI WAR file path, assuming there's only one WAR file
WEBUI_WAR_PATH=`dirname $0`/gs-webui*.war

echo webui war file path: ${WEBUI_WAR_PATH}

if [ "${WEBUI_PORT}" = "" ] ; then
  WEBUI_PORT=8999
fi

${COMMAND_LINE} -name webui -path ${WEBUI_WAR_PATH} -work `dirname $0`/work -port $WEBUI_PORT $*
