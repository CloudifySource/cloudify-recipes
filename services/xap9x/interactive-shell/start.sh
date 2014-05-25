#!/bin/bash
# These are essentials for cleaning the environment of Cloudify
CLASSPATH=
bootclasspath=
CLASS=
COMMAND=
CP=
EXT_JARS=
#EXT_JAVA_OPTIONS=
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
#RMI_OPTIONS=
LOOKUP_LOCATORS_PROP=
LOOKUP_GROUPS_PROP=
command_line=
startParm=
launchTarget=
options=

# args:
# $1 the error code of the last command (should be explicitly passed)
# $2 the message to print in case of an error
#
# an error message is printed and the script exists with the provided error code
function error_exit {
	echo "$2 : error code: $1"
	exit ${1}
}
source /tmp/virtenv_is/bin/activate
source ~/.bashrc
python `pwd`/butterfly/butterfly.server.py --host="0.0.0.0" --port="$BF_UI_PORT" --unsecure --prompt_login=false --load_script="$BF_SCRIPT" --wd="$BF_WORKING_DIRECTORY" $UUID || error_exit $? "Failed to start butterfly server"
deactivate