#!/bin/sh -x

# All Rights Reserved * Licensed Materials - Property of IBM
# 5724-I63, 5724-H88, 5655-N01, 5733-W60 (C) COPYRIGHT International Business Machines Corp., 1997,2005
# US Government Users Restricted Rights - Use, duplication or disclosure
# restricted by GSA ADP Schedule Contract with IBM Corp.

# Configuration Based Server Launcher

# Launch Arguments:
#
# serverName     - the name of the server process to be launched.
# -script [script_file_name]
# -nowait
# -quiet
# -trace
# -timeout <time>
# -statusport <port>
#

set_script_executable()
{
  scriptfile=start_$1.sh
  while [ "$#" -gt "0" ]
  do
    # check for -script option
    if [ "$1" = "-script" ]
    then
       # check the next argument for explicit script name
       if [ $# -gt "1" ]
       then
          shift
          # if the argument begins with "-", ignore it
          # because it is not a script file name
          if [ `echo "$1" | cut -c 1` != "-" ]
          then
             scriptfile="$1"
          fi
       fi
       # make sure the file does exist before setting exec permission
       if [ -f $scriptfile ]
       then
          chmod +x $scriptfile
       fi
    fi
    shift
  done
}


# Bootstrap values ...
APP_EXT_ID=com.ibm.ws.management.tools.WsServerLauncher

binDir=`dirname $0`
. $binDir/setupCmdLine.sh

#The following is added through defect 236497.1
CMD_NAME=`basename $0`

if [ -f ${JAVA_HOME}/bin/java ]; then
    JAVA_EXE="${JAVA_HOME}/bin/java"
else
    JAVA_EXE="${JAVA_HOME}/jre/bin/java"
fi

if [ "${INV_PRF_SPECIFIED:=}" != "" ] && [ "$INV_PRF_SPECIFIED" = "true" ]
then
    ${JAVA_EXE} -cp "${WAS_HOME}/lib/commandlineutils.jar" com.ibm.ws.install.commandline.utils.CommandLineUtils -specifiedProfileNotExists -profileName $WAS_PROFILE_NAME
    exit 1
elif [ "${WAS_USER_SCRIPT_FILE_NOT_EXISTS:=}" != "" ] && [ "$WAS_USER_SCRIPT_FILE_NOT_EXISTS" = "true" ]
then
    ${JAVA_EXE} -cp "${WAS_HOME}/lib/commandlineutils.jar" com.ibm.ws.install.commandline.utils.CommandLineUtils -wasUserScriptFileNotExists -wasUserScript $WAS_USER_SCRIPT
    exit 1
elif [ "${NO_DFT_PRF_EXISTS:=}" != "" ] && [ "$NO_DFT_PRF_EXISTS" = "true" ]
then
    ${JAVA_EXE} -cp "${WAS_HOME}/lib/commandlineutils.jar" com.ibm.ws.install.commandline.utils.CommandLineUtils -noDefaultProfile -commandName $CMD_NAME
    exit 1
fi

# For debugging the launcher itself
# WAS_DEBUG="-Djava.compiler=NONE -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=7777"

#zOS
# "$JAVA_HOME"/bin/java -Dfile.encoding=ISO8859-1 -Xnoargsconversion \
#   $DEBUG \
#   -Dws.ext.dirs="$WAS_EXT_DIRS" \
#    $CONSOLE_ENCODING \
#   -Djava.ext.dirs="$JAVA_EXT_DIRS" \
#   -classpath "$WAS_CLASSPATH" \
#   -Dwas.install.root="$WAS_HOME" \
#   -Dwas.serverstart.cell="$WAS_CELL" \
#   -Dwas.serverstart.node="$WAS_NODE" \
#   -Dwas.serverstart.server="$1" \
#   $USER_INSTALL_PROP \
#   $JVM_EXTRA_CMD_ARGS \
#   com.ibm.ws.bootstrap.WSLauncher \
#   $APP_EXT_ID "$CONFIG_ROOT" "$WAS_CELL" "$WAS_NODE" "$@"

#ASV
# "$JAVA_HOME"/bin/java \
#   $DEBUG \
#   -Dws.ext.dirs="$WAS_EXT_DIRS" \
#   -classpath "$WAS_CLASSPATH" \
#   -Dwas.install.root="$WAS_HOME" \
#   $USER_INSTALL_PROP \
#   com.ibm.ws.bootstrap.WSLauncher \
#   $APP_EXT_ID "$CONFIG_ROOT" "$WAS_CELL" "$WAS_NODE" "$@"

# Setup the initial java invocation;
DELIM=" "

export MONGO_HOST=REPLACE_WITH_MONGO_HOST
export MONGO_PORT=REPLACE_WITH_MONGO_PORT


#Common args...
D_ARGS="-Dws.ext.dirs="$WAS_EXT_DIRS" $DELIM -Dwas.install.root="$WAS_HOME" $DELIM -Djava.util.logging.manager=com.ibm.ws.bootstrap.WsLogManager $DELIM -Djava.util.logging.configureByServer=true"

D_ARGS="-DMONGO_PORT="$MONGO_PORT" $DELIM -DMONGO_HOST="$MONGO_HOST" $DELIM $D_ARGS"

#Platform specific args...
PLATFORM=`/bin/uname`
case $PLATFORM in
  AIX)
    EXTSHM=ON
    LIBPATH="$WAS_LIBPATH":$LIBPATH
    export LIBPATH EXTSHM ;;
  Linux)
    LD_LIBRARY_PATH="$WAS_LIBPATH":$LD_LIBRARY_PATH
    export LD_LIBRARY_PATH ;;
  SunOS)
    LD_LIBRARY_PATH="$WAS_LIBPATH":$LD_LIBRARY_PATH
    export LD_LIBRARY_PATH ;;
  HP-UX)
    SHLIB_PATH="$WAS_LIBPATH":$SHLIB_PATH
    export SHLIB_PATH ;;
  OS/390)
    PATH="$PATH":$binDir
    export PATH
    D_ARGS=""$D_ARGS" $DELIM -Dfile.encoding=ISO8859-1 $DELIM -Djava.ext.dirs="$JAVA_EXT_DIRS""
    D_ARGS=""$D_ARGS" $DELIM -Dwas.serverstart.cell="$WAS_CELL""
    D_ARGS=""$D_ARGS" $DELIM -Dwas.serverstart.node="$WAS_NODE""
    D_ARGS=""$D_ARGS" $DELIM -Dwas.serverstart.server="$1""
    X_ARGS="-Xnoargsconversion" ;;
esac

PATH=${WAS_DB2_PATH_VAR:+"$WAS_DB2_PATH_VAR":}"$PATH"
export PATH

"$JAVA_EXE" \
  "$OSGI_INSTALL" "$OSGI_CFG" \
  $X_ARGS \
  $WAS_DEBUG \
  $CONSOLE_ENCODING \
  $D_ARGS \
  -classpath "$WAS_CLASSPATH" \
  $USER_INSTALL_PROP \
  $JVM_EXTRA_CMD_ARGS \
  com.ibm.ws.bootstrap.WSLauncher \
  $APP_EXT_ID "$CONFIG_ROOT" "$WAS_CELL" "$WAS_NODE" "$@" $WORKSPACE_ROOT_PROP

launchExit=$?
if [ "$launchExit" = "0" ]
then
  set_script_executable $@
fi
exit `expr $launchExit + $?`
~