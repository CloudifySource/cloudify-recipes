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
import org.cloudifysource.usm.USMUtils
import org.cloudifysource.dsl.context.ServiceContextFactory
import static mysql_runner.*

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=USMUtils.isWindows() ? config.win64 : config.linux
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
				runMysqlAdmin(binFolder,osConfig.mysqladmin,currOsName,currActionQuery,currActionDbName,currActionUser,currDebugMsg)
				break
			case "mysql":
				currActionQuery = currActionObj["actionQuery"]
				currActionQuery = currActionQuery.replaceAll("MYSQLHOST",mysqlHost)
				runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,currActionQuery,currActionDbName,currActionUser,currDebugMsg)
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

/* Restart does not require postStart, so if attr postStartRequired is false , postStart will NOT be executed. */
def postStartRequired = context.attributes.thisInstance["postStartRequired"]

println "mysql_start.groovy: postStartRequired is ${postStartRequired}"

if ( postStartRequired ) { 
	try {		
		println "mysql_start.groovy: postStart"
		runPostStartActions(config,binFolder,osConfig,currOsName,builder,context,mysqlHost)
	} 
	catch (Exception ioe) {
		println "mysql_start.groovy: Connection Failed!"
		println ioe
	} 	
}	
	
context.attributes.thisInstance["postStartRequired"] = false	
println "mysql_start.groovy: End"
	

