# These are essentials for cleaning the environment of Cloudify
CLASSPATH=
bootclasspath=
CLASS=
COMMAND=
CP=
EXT_JARS=
EXT_JAVA_OPTIONS=
GS_JARS=
GS_LIB=
GS_LOGGING_CONFIG_FILE=
GS_LOGGING_CONFIG_FILE_PROP=
JAVA_OPTIONS=
JSHOMEDIR=
LOOKUPGROUPS=
SIGAR_JARS=
PRE_CLASSPATH=
POST_CLASSPATH=
AGENT_ID=
GSA_SERVICE_ID=
ENABLE_DYNAMIC_LOCATORS=
RMI_OPTIONS=
LOOKUP_LOCATORS_PROP=
LOOKUP_GROUPS_PROP=
command_line=
startParm=
launchTarget=
options=

ulimit -n 32000
ulimit -u 32000

$XAPDIR/bin/gs-agent.sh gsa.global.lus=0 gsa.lus=1 gsa.global.gsm=1 gsa.gsc=0 &
$XAPDIR/bin/gs-webui.sh &

sleep 10000d
