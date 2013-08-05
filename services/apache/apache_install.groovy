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
import org.hyperic.sigar.OperatingSystem
import java.util.concurrent.TimeUnit
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.domain.context.ServiceInstance;

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("apache-service.properties").toURL())

osConfig = ServiceUtils.isWindows() ? config.win32 : config.linux

downloadFile="${config.downloadFolder}/{$osConfig.zipName}"

def installLinuxHttpd(context,builder,currVendor,installScript,dbType) {
	builder.sequential {
		echo(message:"apache_install.groovy: Chmodding +x ${context.serviceDirectory} ...")
		chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")

		echo(message:"apache_install.groovy: Running ${context.serviceDirectory}/${installScript} os is ${currVendor}...")
		exec(executable: "${context.serviceDirectory}/${installScript}",failonerror: "true") {
			arg(value:"${config.php}")			
			arg(value:"${dbType}")
		}
	}
}

def installWindowsHttpd(config,osConfig,downloadFile,builder,dbType) {
	downloadFolder=System.properties["user.home"]+ "/.cloudify"
	zipsDir = "${downloadFolder}/apache"
	downloadFile = "${zipsDir}/apache2.zip"
	installFolder="${context.serviceDirectory}/install"
	apacheRootFolder="${installFolder}/Apache2"
	
	builder.sequential {
			echo(message:"apache_install.groovy: Creating zipsDir ${zipsDir} ...")
			mkdir(dir:"${zipsDir}")
			echo(message:"apache_install.groovy: installing on Windows...")
			echo(message:"apache_install.groovy: Creating installFolder ${installFolder} ...")
			mkdir(dir:"${installFolder}")
			mkdir(dir:"${downloadFolder}")
			echo(message:"apache_install.groovy: Getting ${osConfig.downloadUrl} to ${downloadFile} ...")
			get(src:"${osConfig.downloadUrl}", dest:"${downloadFile}", skipexisting:true)
			unzip(src:"${downloadFile}", dest:"${installFolder}", overwrite:true)
			copy( todir:"${installFolder}" ) {
				fileset( dir:'overrides-win' )
			}	
	}
	
	if ( "${config.php}" == "true" ) {
		// Need to add php implementation here if required
	
		phpZipFile = "${zipsDir}/php.zip"
		phpRootFolder="${installFolder}/php"		

		builder.sequential {
			echo(message:"apache_install.groovy: Creating folder for phpRootFolder - ${phpRootFolder} ...")
			mkdir(dir:"${phpRootFolder}")
			echo(message:"apache_install.groovy: Getting ${osConfig.phpDownloadUrl} to ${phpZipFile} ...")
			get(src:"${osConfig.phpDownloadUrl}", dest:"${phpZipFile}", skipexisting:true)
			echo(message:"apache_install.groovy: Unzipping ${phpZipFile} to ${installFolder} ...")
			unzip(src:"${phpZipFile}", dest:"${phpRootFolder}", overwrite:true)
		}		
	}
}

def useDB
def dbHost = "" 
def dbPort
def dbType = ""

if ( !(config.dbServiceName) ||  "${config.dbServiceName}"=="NO_DB_REQUIRED") {
	println "apache_install.groovy: Your application doesn't use a database"
	useDB=false
}
else { 
	useDB = true
		
	if ( !(config.dbHost) ||  "${config.dbHost}"=="DB_INSTALLED_BY_CLOUDIFY") {
		dbService = context.waitForService(config.dbServiceName, 360, TimeUnit.SECONDS)
		if (dbService == null) {
			throw new IllegalStateException("${config.dbServiceName} service not found.");
		}
		ServiceInstance[] dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 360, TimeUnit.SECONDS)

		if (dbInstances == null) {
			throw new IllegalStateException("dbInstances not found.");
		}

		dbHost = dbInstances[0].getHostAddress()
		println "apache_install.groovy: dbHost is ${dbHost}"

		def dbServiceInstances=context.attributes[config.dbServiceName].instances                   
		dbServiceInstance=dbServiceInstances[1]				
		dbPort=dbServiceInstance["dbPort"]
		println "apache_install.groovy: dbPort is ${dbPort}"
	}
	else {
		dbHost = config.dbHost
		println "apache_install.groovy: Using (external db) dbHost : ${dbHost}"
		dbPort = config.dbPort
		println "apache_install.groovy: Using (external db) dbPort : ${dbPort}"
	}	
}

context.attributes.thisInstance["dbHost"] = dbHost


if ( useDB ) {
		
	if ( "${config.dbServiceName}" == "mysql" ) { 
		dbType = "mysql"
	}
	else {
		println "apache_install.groovy: You need to implement code for another database according to your db type : ${config.dbServiceName} (e.g. : for postgres)"
	}
	
	dbUserStr="${config.dbUser}"
	dbPassStr="${config.dbPassW}"			
}

context.attributes.thisInstance["useDB"]=useDB
context.attributes.thisInstance["dbType"]=dbType


builder = new AntBuilder()

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
		case ["Ubuntu", "Debian", "Mint"]:			
			installLinuxHttpd(context,builder,currVendor,"installOnUbuntu.sh",dbType)
			context.attributes.thisInstance["docRoot"]="/var/www"
			break		
		case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:			
			installLinuxHttpd(context,builder,currVendor,"install.sh",dbType)			
			context.attributes.thisInstance["docRoot"]="/var/www/html"							
			break					
		case ~/.*(?i)(Microsoft|Windows).*/:		
			installWindowsHttpd(config,osConfig,downloadFile,builder,dbType)			
			break
		default: throw new Exception("Support for ${currVendor} is not implemented")
}

