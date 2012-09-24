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
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.hyperic.sigar.OperatingSystem


def installLinuxMysql(context,builder,currVendor,installScript,myCnfObject) {

	if ( context.isLocalCloud() ) {
		if ( context.attributes.thisApplication["installing"] == null || context.attributes.thisApplication["installing"] == false ) {
			context.attributes.thisApplication["installing"] = true
		}
		else {
			while ( context.attributes.thisApplication["installing"] == true ) {
				println "mysql_install.groovy: Waiting for apt-get/yum (on localCloud) to end on another service instance in this application... "
				sleep 10000			
			}
		}
	}

	def sectionNames = myCnfObject["sectionNames"]
	def variableNames = myCnfObject["variableNames"]
	def newValues = myCnfObject["newValues"]
	
	
	builder.sequential {
		echo(message:"mysql_install.groovy: Chmodding +x ${context.serviceDirectory} ...")
		chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")

		echo(message:"mysql_install.groovy: Running ${context.serviceDirectory}/${installScript} os is ${currVendor}...")
		exec(executable: "${context.serviceDirectory}/${installScript}",failonerror: "true") {
			arg(value:"${sectionNames}")		
			arg(value:"${variableNames}")		
			arg(value:"${newValues}")		
		}
	}
	
	if ( context.isLocalCloud() ) {
		context.attributes.thisApplication["installing"] = false
		println "mysql_install.groovy: Finished using apt-get/yum on localCloud"
	}
}

def installWindowsMysql(config,osConfig,unzipDir,zipFullPath,builder,myCnfObject) {
	builder.sequential {	
		mkdir(dir:"${unzipDir}")
		echo(message: "mysql_install.groovy get ${osConfig.zipURL} ... ")
		get(src:"${osConfig.zipURL}", dest:"${zipFullPath}", skipexisting:true)
		echo(message:"mysql_install.groovy: Unzipping ${zipFullPath} to ${context.serviceDirectory}...")
		unzip(src:"${zipFullPath}", dest:"${context.serviceDirectory}", overwrite:true)
	}
}

def getCnfObject(config,context,isMaster,isSlave) {

	def sectionNames = ""
	def variableNames = "" 
	def newValues = "" 
	
	def varaiblesCounter = 1
	
	def newMyCnfObject = [
		"sectionNames" : "" ,
		"variableNames" : "",
		"newValues" : ""
	]
	
	
	if ( config.masterSlaveMode ) {
		println "mysql_install.groovy: getCnfObject: masterSlaveMode"
			
		if ( isMaster ) {
			println "mysql_install.groovy: getCnfObject: variables set #${varaiblesCounter} (server-id)"
			sectionNames +="mysqld,"
			variableNames +="server-id,"
			newValues +="1,"	
			++varaiblesCounter
		}
		else if ( isSlave ) {
			// id 1 is for the master. So the slaves instances will start from id #2
			println "mysql_install.groovy: getCnfObject: variables set #${varaiblesCounter} (server-id)"
			def slaveServerID = context.getInstanceId()+1
			sectionNames +="mysqld,"
			variableNames +="server-id,"
			newValues +="${slaveServerID},"
			++varaiblesCounter
		}
	}
	
	def myCnfReplacements
	if ( config.myCnfReplacements ) {
		myCnfReplacements = config.myCnfReplacements
		if ( myCnfReplacements.size > 0 ) { 
			println "mysql_install.groovy: getCnfObject: There are " +myCnfReplacements.size + " myCnfReplacements " 
			for ( currActionObj in myCnfReplacements ) { 
				def currSection = currActionObj["section"]
				def currVariable = currActionObj["variable"]
				def currNewValue = currActionObj["newValue"]
				sectionNames +=currSection+","
				variableNames +=currVariable+","
				newValues +=currNewValue+","
				println "mysql_install.groovy: getCnfObject: variables set #${varaiblesCounter} (${currVariable})"
				++varaiblesCounter
			}
		}
	}

	
	if ( sectionNames.length() > 0 && sectionNames.endsWith(",")) {
		newMyCnfObject.put("sectionNames",sectionNames[0..-2])
		newMyCnfObject.put("variableNames",variableNames[0..-2])
		newMyCnfObject.put("newValues",newValues[0..-2])
		println "mysql_install.groovy: getCnfObject: mycnf sectionNames is "+newMyCnfObject["sectionNames"]
		println "mysql_install.groovy: getCnfObject: mycnf variableNames is "+newMyCnfObject["variableNames"]
		println "mysql_install.groovy: getCnfObject: mycnf newValues is "+newMyCnfObject["newValues"]
	}
		

	return newMyCnfObject
}

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=ServiceUtils.isWindows() ? config.win64 : config.linux

context = ServiceContextFactory.getServiceContext()
def myInstanceID=context.getInstanceId()

def isMaster=false
def isSlave=false

if ( config.masterSlaveMode ) {	
	println "mysql_install.groovy: masterSlaveMode"
	if ( myInstanceID == 1 ) {
		println "mysql_install.groovy: I am a master b4 setting isMaster to true and isSlave to false..."
		isMaster = true		
		context.attributes.thisService["masterIsReady"]=false
		context.attributes.thisService["masterID"]=myInstanceID
		isSlave = false
	}
	else {
		println "mysql_install.groovy: I am a slave(instance id ${myInstanceID}) b4 setting isSlave to true and isMaster to false..."
		isMaster = false
		isSlave = true		
	}
	
	context.attributes.thisInstance["isSlave"]=isSlave
	context.attributes.thisInstance["isMaster"]=isMaster		
	
}
else {
	println "mysql_install.groovy: Installing in a standalone mode"
}

def mysqlHost

if (  context.isLocalCloud()  ) {
	mysqlHost =InetAddress.getLocalHost().getHostAddress()
}
else {
	mysqlHost =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
}


println "mysql_install.groovy: mysqlHost is ${mysqlHost}"
context.attributes.thisInstance["dbHost"] = "${mysqlHost}"

context.attributes.thisInstance["dbName"] = "${config.dbName}"
println "mysql_install.groovy: dbName is ${config.dbName}"

context.attributes.thisInstance["dbUser"] = "${config.dbUser}"
println "mysql_install.groovy: dbUser is ${config.dbUser}"

context.attributes.thisInstance["dbPassW"] = "${config.dbPassW}"
println "mysql_install.groovy: dbPassW is ${config.dbPassW}"

context.attributes.thisInstance["dbPort"] = "${config.jdbcPort}"
println "mysql_install.groovy: dbPort is ${config.jdbcPort}"

context.attributes.thisInstance["postStartRequired"] = true

def myCnfObject = getCnfObject(config,context,isMaster,isSlave)

builder = new AntBuilder()

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
		case ["Ubuntu", "Debian", "Mint"]:		
			context.attributes.thisInstance["binFolder"]="/usr/bin"
			installLinuxMysql(context,builder,currVendor,"installOnUbuntu.sh",myCnfObject)
			break		
		case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:	
			context.attributes.thisInstance["binFolder"]="/usr/bin"		
			installLinuxMysql(context,builder,currVendor,"install.sh",myCnfObject)
			break					
		case ~/.*(?i)(Microsoft|Windows).*/:
			context.attributes.thisInstance["binFolder"]="${osConfig.mysqlHome}/bin"
			unzipDir = System.properties["user.home"]+ "/.cloudify/mysql"
			zipFullPath="${unzipDir}/${osConfig.zipName}"
			installWindowsMysql(config,osConfig,unzipDir,zipFullPath,builder,myCnfObject)
			break
		default: throw new Exception("Support for ${currVendor} is not implemented")
}

println "mysql_install.groovy: End"

