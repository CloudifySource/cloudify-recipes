@echo off
rem 	WEBUI_JAVA_OPTIONS 	- Extended java options that are proprietary defined  for Web UI such as heap size, system properties or other JVM arguments that can be passed to the JVM command line. 
rem							- These settings can be overridden externally to this script.

title GigaSpaces Web-UI

@set JSHOMEDIR=%~dp0\..\..

@rem set WEBUI_JAVA_OPTIONS=
set COMPONENT_JAVA_OPTIONS=%WEBUI_JAVA_OPTIONS%

@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\..\..\bin\setenv.bat"

set LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=%LOOKUPGROUPS%

if "%LOOKUPLOCATORS%" == ""  (
set LOOKUPLOCATORS=
)
set LOOKUP_LOCATORS_PROP=-Dcom.gs.jini_lus.locators=%LOOKUPLOCATORS%

set JETTY_JARS="%JSHOMEDIR%\lib\platform\jetty\*"

set COMMAND_LINE=%JAVACMD% %JAVA_OPTIONS% %bootclasspath% %LOOKUP_LOCATORS_PROP% %LOOKUP_GROUPS_PROP% %GS_LOGGING_CONFIG_FILE_PROP% %RMI_OPTIONS% "-Dcom.gs.home=%JSHOMEDIR%" -Djava.security.policy=%POLICY% -Dcom.gigaspaces.logger.RollingFileHandler.time-rolling-policy=monthly -classpath %PRE_CLASSPATH%;%GS_JARS%;%EXT_JARS%;%JETTY_JARS%;%POST_CLASSPATH% org.openspaces.launcher.Launcher

if "%WEBUI_PORT%" == "" (
set WEBUI_PORT=8999
)
%COMMAND_LINE%  -name webui -path "%~dp0/gs-webui.war" -work "%~dp0/work" -port %WEBUI_PORT%  %*
