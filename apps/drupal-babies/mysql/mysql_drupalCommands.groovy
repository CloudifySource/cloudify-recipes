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
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;
import static mysql_runner.*

println "mysql_drupalCommands.groovy: Starting ..."

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=ServiceUtils.isWindows() ? config.win64 : config.linux
context = ServiceContextFactory.getServiceContext()
def mysqlHost=context.attributes.thisInstance["dbHost"]
def binFolder=context.attributes.thisInstance["binFolder"]
println "mysql_drupalCommands.groovy: mysqlHost is ${mysqlHost} "
println "mysql_drupalCommands.groovy: binFolder is ${binFolder} "

def drupalVersion=context.attributes.thisApplication["drupalVersion"]
println "mysql_drupalCommands.groovy: drupalVersion is ${drupalVersion}"
def currCommand=args[0]

def currOsName="unix"

def deleteD6Cache(binFolder,osConfig,config,currOsName) {
	//mysql -u user -ppassword -D dbname -e "\"DELETE FROM cache WHERE CID = 'variables';\""	
	deleteCacheCmnd= "\"DELETE FROM cache WHERE CID = 'variables';\""
	currDebugMsg= "Deleting cache : ${deleteCacheCmnd} ... "
	runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,deleteCacheCmnd,"${config.dbName}","${config.dbUser}","${config.dbPassW}",currDebugMsg,"queryOutput",true)	
}

def deleteD7Cache(binFolder,osConfig,config,currOsName) {
	//mysql -u user -ppassword -D dbname -e "\"delete from cache_bootstrap where cid = 'variables';\""	
	deleteCacheCmnd= "\"delete from cache_bootstrap where cid = 'variables';\""
	currDebugMsg= "Deleting cache : ${deleteCacheCmnd} ... "
	runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,deleteCacheCmnd,"${config.dbName}","${config.dbUser}","${config.dbPassW}",currDebugMsg,"queryOutput",true)	

	//mysql -u user -ppassword -D dbname -e "\"truncate table cache_page;\""	
	deleteCacheCmnd= "\"truncate table cache_page;\""
	currDebugMsg= "Deleting cache : ${deleteCacheCmnd} ... "
	runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,deleteCacheCmnd,"${config.dbName}","${config.dbUser}","${config.dbPassW}",currDebugMsg,"queryOutput",true)		
}

def activateSite(activateQuery,drupalVersion,binFolder,osConfig,config,currOsName) {
	try {	
		println "mysql_drupalCommands.groovy: In activateSite..."
		currDebugMsg= "Activating Drupal ${drupalVersion} site- Invoking ${activateQuery} ..."	
		runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,activateQuery,"${config.dbName}","${config.dbUser}","${config.dbPassW}",currDebugMsg,"queryOutput",true)
		( "${drupalVersion}" == "6" )?deleteD6Cache(binFolder,osConfig,config,currOsName):deleteD7Cache(binFolder,osConfig,config,currOsName)
	}
	catch (Exception ioe) {
		println "mysql_drupalCommands.groovy: activateSite: Connection Failed!"
		ioe.printStackTrace();
	} 
	println "mysql_drupalCommands.groovy: End of activateSite"
}


def activateD6Site(binFolder,osConfig,config,currOsName) {
	println "mysql_drupalCommands.groovy: in activateD6Site ..."
	activeValueD6= "concat('s:1:',char(34),'0' ,char(34),';')"
	//mysql -u user -ppassword -D dbname -e "INSERT INTO variable (name,value) values('site_offline','${activeValueD6}') ON DUPLICATE KEY UPDATE value='${activeValueD6}';"		
	activateQuery= "\"INSERT INTO variable (name,value) values('site_offline',${activeValueD6}) ON DUPLICATE KEY UPDATE value=${activeValueD6};\""
	activateSite(activateQuery,"6",binFolder,osConfig,config,currOsName)
	println "mysql_drupalCommands.groovy: End of activateD6Site"	
}

def activateD7Site(binFolder,osConfig,config,currOsName) {
	println "mysql_drupalCommands.groovy: in activateD7Site ..."
	//mysql -u user -ppassword -D dbname -e "\"update variable set value = 'i:0;' where name = 'maintenance_mode';\""	
	activateQuery= "\"update variable set value = 'i:0;' where name = 'maintenance_mode';\""
	activateSite(activateQuery,"7",binFolder,osConfig,config,currOsName)
	println "mysql_drupalCommands.groovy: End of activateD7Site"	
}

def siteOffline(setOffLineCmnd,drupalVersion,binFolder,osConfig,config,currOsName) {
	try {	
		println "mysql_drupalCommands.groovy: In siteOffline ..."
		currDebugMsg= "Setting Drupal ${drupalVersion} site to offline - Invoking ${setOffLineCmnd} ..."
		runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,setOffLineCmnd,"${config.dbName}","${config.dbUser}","${config.dbPassW}",currDebugMsg,"queryOutput",true)
		( "${drupalVersion}" == "6" )?deleteD6Cache(binFolder,osConfig,config,currOsName):deleteD7Cache(binFolder,osConfig,config,currOsName)
	}
	catch (Exception ioe) {
		println "mysql_drupalCommands.groovy: siteOffline: Connection Failed!"
		ioe.printStackTrace();
	} 
	println "mysql_drupalCommands.groovy: End of siteOffline"
}

def d6SiteOffline(binFolder,osConfig,config,currOsName) {
	println "mysql_drupalCommands.groovy: in d6SiteOffline ..."
	offlineValueD6 = "concat('s:1:',char(34),'1' ,char(34),';')"
	//mysql -u root -ppassword -D dbname -e "INSERT INTO variable (name,value) values('site_offline','${offlineValueD6}') ON DUPLICATE KEY UPDATE value='${offlineValueD6}';"		
	setOffLineCmnd= "\"INSERT INTO variable (name,value) values('site_offline',${offlineValueD6}) ON DUPLICATE KEY UPDATE value=${offlineValueD6};\""
	siteOffline(setOffLineCmnd,"6",binFolder,osConfig,config,currOsName)
	println "mysql_drupalCommands.groovy: End of d6SiteOffline"
}

def d7SiteOffline(binFolder,osConfig,config,currOsName) {
	println "mysql_drupalCommands.groovy: in d7SiteOffline ..."
	//mysql -u root -ppassword -D dbname -e "insert into variable (value,name) values('i:1;','maintenance_mode') ON DUPLICATE KEY UPDATE value='i:1;';"
	setOffLineCmnd= "\"insert into variable (value,name) values('i:1;','maintenance_mode') ON DUPLICATE KEY UPDATE value='i:1;';\""
	siteOffline(setOffLineCmnd,"7",binFolder,osConfig,config,currOsName)	
	println "mysql_drupalCommands.groovy: End of d7SiteOffline"
}

switch (currCommand) {
	case ["activateSite"]:		
		( "${drupalVersion}" == "6" )?activateD6Site(binFolder,osConfig,config,currOsName):activateD7Site(binFolder,osConfig,config,currOsName)
		break		
	case ["siteOffline"]:	
		( "${drupalVersion}" == "6" )?d6SiteOffline(binFolder,osConfig,config,currOsName):d7SiteOffline(binFolder,osConfig,config,currOsName)
		break
	case ["deleteCache"]:	
		( "${drupalVersion}" == "6" )?deleteD6Cache(binFolder,osConfig,config):deleteD7Cache(binFolder,osConfig,config)
		break		
	default: throw new Exception("Support for ${currCommand} command is not implemented")
}







	
println "mysql_drupalCommands.groovy: End"
