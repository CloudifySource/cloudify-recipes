#!/bin/bash
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

source /tmp/virtenv_xapman/bin/activate
python `pwd`/butterfly/butterfly.server.py --host="0.0.0.0" --port="$BF_UI_PORT" --unsecure --prompt_login=false $UUID &
deactivate