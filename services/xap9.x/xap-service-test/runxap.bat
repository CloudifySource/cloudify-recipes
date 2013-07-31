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

echo start >>c:\Users\DeWayne\Documents\out.txt
echo USM_INSTANCEID %USM_INSTANCEID% >> c:\Users\DeWayne\Documents\out.txt

SET MANAGEMENT_NODE=false
if %USM_INSTANCEID%==1 SET MANAGEMENT_NODE=true
if %USM_INSTANCEID%==2 SET MANAGEMENT_NODE=true

echo USM_INSTANCEID %USM_INSTANCEID% >> c:\Users\DeWayne\Documents\out.txt
echo MANAGEMENT_NODE %MANAGEMENT_NODE% >> c:\Users\DeWayne\Documents\out.txt
echo ISLOCALCLOUD %ISLOCALCLOUD% >> c:\Users\DeWayne\Documents\out.txt

if %ISLOCALCLOUD% ==true (
	if %USM_INSTANCEID% == 1 (
echo "LOCALCLOUD INSTANCE 1">> c:\Users\DeWayne\Documents\out.txt
		start /b %XAPDIR%\bin\gsm.bat
		start /b %XAPDIR%\bin\gsc.bat
		cmd /c %XAPDIR%\bin\gs-webui.bat
	) ELSE (
echo "LOCALCLOUD INSTANCE >1" >> c:\Users\DeWayne\Documents\out.txt
		cmd /c %XAPDIR%\bin\gsc.bat
	)	
) ELSE (
	if %MANAGEMENT_NODE% == true (
echo "STARTING MANAGEMENT NODE">> c:\Users\DeWayne\Documents\out.txt
		start /b %XAPDIR%\bin\gsm.bat
		cmd /c %XAPDIR%\bin\gs-webui.bat
	) ELSE (	
echo "STARTING CONTAINER NODE">> c:\Users\DeWayne\Documents\out.txt
		cmd /c %XAPDIR%\bin\gsc.bat
	)
)

