SET CLASSPATH=
SET bootclasspath=
SET CLASS=
SET COMMAND=
SET CP=
SET EXT_JARS=
SET EXT_JAVA_OPTIONS=
SET GS_JARS=
SET GS_LIB=
SET GS_LOGGING_CONFIG_FILE=
SET GS_LOGGING_CONFIG_FILE_PROP=
SET JAVA_OPTIONS=
SET JSHOMEDIR=
SET LOOKUPGROUPS=
SET SIGAR_JARS=
SET PRE_CLASSPATH=
SET POST_CLASSPATH=
SET AGENT_ID=
SET GSA_SERVICE_ID=
SET ENABLE_DYNAMIC_LOCATORS=
SET RMI_OPTIONS=
SET LOOKUP_LOCATORS_PROP=
SET LOOKUP_GROUPS_PROP=
SET command_line=
SET startParm=
SET launchTarget=
SET options=

start /b %XAPDIR%\bin\gs-agent.bat gsa.global.lus=1 gsa.global.gsm=1 gsa.gsc=0
cmd /c %XAPDIR%\bin\gs-webui.bat
