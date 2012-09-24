/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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
import org.hyperic.sigar.OperatingSystem
import java.util.concurrent.TimeUnit
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory
import static mysql_runner.*

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=ServiceUtils.isWindows() ? config.win64 : config.linux
context = ServiceContextFactory.getServiceContext()
mysqlHost=context.attributes.thisInstance["dbHost"]
binFolder=context.attributes.thisInstance["binFolder"]
println "mysql_start.groovy: mysqlHost is ${mysqlHost} "


def runPostStartActions(config,binFolder,osConfig,currOsName,builder,context,mysqlHost) {	
	def currActionQuery 
	def actionCounter = 1
	
	for ( currActionObj in config.postStartActions ) { 	
		println "mysql_start.runPostStartActions: postStartActions #${actionCounter}:"
		def currActionType = currActionObj["actionType"]
		def currActionUser = currActionObj["actionUser"]
		def currActionDbName = currActionObj["actionDbName"]
		def currDebugMsg = currActionObj["debugMsg"]
		currDebugMsg = currDebugMsg.replaceAll("MYSQLHOST",mysqlHost)
		
		switch (currActionType) {
			case "mysqladmin":
				currActionQuery = currActionObj["actionQuery"]
				currActionQuery = currActionQuery.replaceAll("MYSQLHOST",mysqlHost)
				runMysqlAdmin(binFolder,osConfig.mysqladmin,currOsName,currActionQuery,currActionDbName,currActionUser,currDebugMsg,"mysqladminOutput",true)
				break
			case "mysql":
				currActionQuery = currActionObj["actionQuery"]
				currActionQuery = currActionQuery.replaceAll("MYSQLHOST",mysqlHost)
				runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,currActionQuery,currActionDbName,currActionUser,currDebugMsg,"mysqlOutput",true)
				break
			case "import":
				def currImportZip="${context.serviceDirectory}/"+currActionObj["importedZip"]
				def importedFile=currActionObj["importedFile"]
				def importedFileUrl=currActionObj["importedFileUrl"]
				importFileToDB(binFolder,osConfig,currOsName,currActionDbName,currImportZip,importedFile,importedFileUrl,builder,context,currActionUser,currDebugMsg,config)
			  break
			case "mysqldump":
				currActionArgs = currActionObj["actionArgs"]
				currDumpPrefix = currActionObj["dumpPrefix"]
				def dumpFolder = System.properties["user.home"]
			    runMysqlDump(binFolder,osConfig.mysqldump,currOsName,currActionArgs,currActionDbName,currActionUser,currDebugMsg,dumpFolder,currDumpPrefix)							
				break			  
			default:
			  println "Ignoring Action Type ${currActionType} ... "
			  break
		}
		
		actionCounter++
	}
}


def addSlaveToMaster(context,config,mysqlHost) {
	
	while ( !context.attributes.thisService["masterIsReady"] ) {
		println "mysql_start.groovy: addSlaveToMaster: Slave is waiting for the master..."
		sleep 10000			
	}
	
	println "mysql_start.groovy: addSlaveToMaster: Waiting for my future master ..."
	def mysqlService = context.waitForService("mysql", 180, TimeUnit.SECONDS)
	def masterInstances = mysqlService.waitForInstances(mysqlService.numberOfActualInstances, 180, TimeUnit.SECONDS)
	
	def index
	def currInstance
	def masterInstance
	def masterHostAddress
	def masterID = context.attributes.thisService["masterID"]
	def instancesCount = masterInstances.length
	println "mysql_start.groovy: addSlaveToMaster: ${instancesCount} instances are available..."
	for (index=0; index < instancesCount; index++) { 
		println "mysql_start.groovy: addSlaveToMaster: In loop b4 index ${index} ... "
		currInstance = masterInstances[index]
		println "mysql_start.groovy: addSlaveToMaster: In loop after index ${index} currInstance.getInstanceID = " + currInstance.getInstanceID() 
		if ( masterID == currInstance.getInstanceID()  ) {
			masterHostAddress = currInstance.hostAddress
			masterInstance = currInstance
			println "mysql_start.groovy: addSlaveToMaster: master instance id is ${masterID}"
			break;
		}
	}
	
	println "mysql_start.groovy: masterHostAddress is ${masterHostAddress}"
	println "mysql_start.groovy: addSlaveToMaster: About to invoke addSlave on the master (instance # ${masterID}) ... - Using dbName ${config.dbName}, user ${config.dbUser}, mysqlHost ${mysqlHost} ... "
	def currActionUser = "root"
	def currDbName = "${config.dbName}"
	def currDbUser = "${config.dbUser}"
	def currDbPassw = "${config.dbPassW}"
	/* This will grant replication permissions to the slave. */
	def currResult = masterInstance.invoke("addSlave", currActionUser as String, currDbName as String, currDbUser as String, currDbPassw as String, mysqlHost as String)
	println "mysql_start.groovy: addSlaveToMaster : addSlave result is ${currResult}"
	println "mysql_start.groovy: addSlaveToMaster : Invoking addSlave on the master (instance # ${masterID}) ended."
	return masterHostAddress
	
}

def changeMasterTo(config,binFolder,osConfig,currOsName,context,masterHost) {
	println "mysql_start.groovy: In changeMasterTo ..."
	
	while ( !context.attributes.thisService["masterIsReady"] ) {
		println "mysql_start.groovy: changeMasterTo: Slave is waiting for the master..."
		sleep 10000			
	}
	
	def binLog = context.attributes.thisApplication["masterBinLogFile"]
	def logPos = context.attributes.thisApplication["masterBinLogPos"]
	def currActionQuery = "\"" + "CHANGE MASTER TO MASTER_HOST='${masterHost}', MASTER_USER='${config.dbUser}', MASTER_PASSWORD='${config.dbPassW}', MASTER_LOG_FILE='${binLog}', MASTER_LOG_POS=${logPos};" + "\""
	def currDebugMsg = "About to invoke ${currActionQuery}..."
	runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,currActionQuery,"${config.dbName}","root",currDebugMsg,"changeMasterOutput",true)
	println "mysql_start.groovy: changeMasterTo Ended"
}

def startSlave(config,binFolder,osConfig,currOsName) {
	println "mysql_start.groovy: In startSlave ..."
	def currActionQuery = "\"" + "start slave;" + "\""
	def currDebugMsg = "About to invoke ${currActionQuery}..."
	runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,currActionQuery,"${config.dbName}","root",currDebugMsg,"startSlaveOutput",true)
	println "mysql_start.groovy: startSlave Ended"
}

def stopSlave(config,binFolder,osConfig,currOsName) {
	println "mysql_start.groovy: In stopSlave ..."
	def currActionQuery = "\"" + "stop slave;" + "\""
	def currDebugMsg = "About to invoke ${currActionQuery}..."
	runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,currActionQuery,"${config.dbName}","root",currDebugMsg,"startSlaveOutput",true)
	println "mysql_start.groovy: stopSlave Ended"
}



builder = new AntBuilder()

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
		case ["Ubuntu", "Debian", "Mint"]:		
			script="${context.serviceDirectory}/runOnUbuntu.sh"
			currOsName="unix"		
			break		
		case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:
			script="${context.serviceDirectory}/run.sh"
			currOsName="unix"
			break					
		case ~/.*(?i)(Microsoft|Windows).*/:
			script="${binFolder}/${osConfig.mysqlD}"
			currOsName="windows"
			break
		default: throw new Exception("Support for ${currVendor} is not implemented")
}


builder.sequential {	
    echo(message:"mysql_start.groovy: Running ${script} on ${currOsName} ...")
	exec(executable:"${script}", osfamily:"${currOsName}",failonerror: "true")
}

/* Restart does not require postStart, so if the postStartRequired attribute is false , postStart will NOT be executed. */
def postStartRequired = context.attributes.thisInstance["postStartRequired"]

println "mysql_start.groovy: postStartRequired is ${postStartRequired}"

def masterHost

def isSlave = context.attributes.thisInstance["isSlave"]

if ( postStartRequired ) { 
	try {		
		println "mysql_start.groovy: postStart"
		runPostStartActions(config,binFolder,osConfig,currOsName,builder,context,mysqlHost)
		if ( config.masterSlaveMode ) {
			println "mysql_start.groovy: Running in a masterSlaveMode mode"
			if ( isSlave ) {
				println "mysql_start.groovy: I am a slave..."
				masterHost = addSlaveToMaster(context,config,mysqlHost)
				context.attributes.thisInstance["masterHost"] = masterHost
			}
		}
	} 
	catch (Exception ioe) {
		println "mysql_start.groovy: Connection Failed!"
		println ioe
	} 	
}
else {
	if ( config.masterSlaveMode ) {
		if ( isSlave ) {	
			masterHost = context.attributes.thisInstance["masterHost"]
		}
	}
}


if ( config.masterSlaveMode ) {
	println "mysql_start.groovy: masterSlaveMode"
	if ( isSlave ) {
		println "mysql_start.groovy: I am a slave b4 invoking changeMasterTo..."
		changeMasterTo(config,binFolder,osConfig,currOsName,context,masterHost)
		println "mysql_start.groovy: Starting slave threads..."		
		startSlave(config,binFolder,osConfig,currOsName)
	}
	else {
		println "mysql_start.groovy: I am a master..."		
	}
}
else {
	println "mysql_start.groovy: Running in a standalone mode"
}	
	
context.attributes.thisInstance["postStartRequired"] = false	
println "mysql_start.groovy: End"