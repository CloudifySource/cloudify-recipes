/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory




context = ServiceContextFactory.getServiceContext()

/* This means that the content is in the root folder */
context.attributes.thisInstance["zipContentLevel"]="0"
new GroovyShell().evaluate(new File("apache_postInstall.groovy"))  	   	   

config = new ConfigSlurper().parse(new File("drupal-service.properties").toURL())

osConfig = ServiceUtils.isWindows() ? config.win32 : config.linux


builder = new AntBuilder()

unzipDir = System.properties["user.home"]+ "/.cloudify/drupal"
installFolder="${context.serviceDirectory}/install"	

println "drupal_postInstall.groovy: Creating folder ${installFolder} ..."
builder.mkdir(dir:"${installFolder}")


drRoot=context.attributes.thisInstance["docRoot"]

drupalZip="${installFolder}/drupal.zip"
drupalTmpUnzipped="${installFolder}/tmpUnzipped"

drupalDefaultFolder="${drRoot}/sites/default"
drupalSettingsFilePath="${drupalDefaultFolder}/settings.php"

def mysqlInstance=context.attributes.mysql.instances[1]

mySqlHost=mysqlInstance["dbHost"]
dbName=mysqlInstance["dbName"]
dbUser=mysqlInstance["dbUser"]
dbPassW=mysqlInstance["dbPassW"]
dbPort=mysqlInstance["dbPort"]

mySqlOrigConnString="\$db_url = 'mysql://username:password@localhost/databasename';"
newMySqlConnString="\$db_url = 'mysql://${dbUser}:${dbPassW}@${mySqlHost}/${dbName}';"

allModules="${drRoot}/sites/all/modules"
allThemes="${drRoot}/sites/all/themes"

downloadedSiteImage="${installFolder}/${config.siteImageFile}"



builder.sequential {	

	echo(message:"drupal_postInstall.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")
	
	echo(message:"drupal_postInstall.groovy: Running drupalConfigure.sh ...")
	exec(executable: "${context.serviceDirectory}/drupalConfigure.sh",failonerror: "true") {
		arg(value:"${drRoot}")		
		arg(value:"${config.drupalVersion}")	
		arg(value:"${dbName}")	
		arg(value:"${dbUser}")	
		arg(value:"${dbPassW}")	
		arg(value:"${dbPort}")	
		arg(value:"${mySqlHost}")	
	}	
}


currModuleZip="${installFolder}/currModule.zip"

config.importModules.each { 
	println it.key + " : wget " + it.value
	moduleUrl=it.value
	builder.sequential { -> moduleUrl	
		echo(message:"Getting ${moduleUrl} ...")
		get(src:"${moduleUrl}", dest:"${currModuleZip}", skipexisting:false)
		echo(message:"Unzipping ${moduleUrl} to ${allModules} ...")
		unzip(src:"${currModuleZip}", dest:"${allModules}", overwrite:true)
		// If we want it to be enabled prior to accessing it in the admin(web), 
		// then it requires more DB manipulations.
	}
}



currThemeZip="${installFolder}/currTheme.zip"

config.importThemes.each { 
	println it.key + " : wget " + it.value
	themeUrl=it.value
	builder.sequential { -> themeUrl	
		echo(message:"Getting ${themeUrl} ...")
		get(src:"${themeUrl}", dest:"${currThemeZip}", skipexisting:false)
		echo(message:"Unzipping ${themeUrl} to ${allThemes} ...")
		unzip(src:"${currThemeZip}", dest:"${allThemes}", overwrite:true)
		// If we want it to be enabled prior to accessing it in the admin(web), 
		// then it requires more DB manipulations.
	}
}

println "drupal_postInstall.groovy: Storing drupalVersion (${config.drupalVersion}) in context.attributes.thisApplication ... "
context.attributes.thisApplication["drupalVersion"]="${config.drupalVersion}"
println "drupal_postInstall.groovy: Ended successfully"

