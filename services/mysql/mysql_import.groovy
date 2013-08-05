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
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import static mysql_runner.*


/* 
	This custom command enables users to import a zipped file to a database
	Usage :  invoke import actionUser dbName zipFileURL
	Example: invoke import root myDbName http://www.mysite.com/myFile.zip
*/

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=ServiceUtils.isWindows() ? config.win64 : config.linux
context = ServiceContextFactory.getServiceContext()
mysqlHost=context.attributes.thisInstance["dbHost"]
binFolder=context.attributes.thisInstance["binFolder"]
println "mysql_import.groovy: mysqlHost is ${mysqlHost} "

def currActionQuery 
def currOsName

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
		case ["Ubuntu", "Debian", "Mint"]:			
			currOsName="unix"		
			break		
		case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:			
			currOsName="unix"
			break					
		case ~/.*(?i)(Microsoft|Windows).*/:			
			currOsName="windows"
			break
		default: throw new Exception("Support for ${currVendor} is not implemented")
}


if (args.length < 3) {
	println "mysql_import.groovy: import error: Missing parameters\nUsage: invoke import actionUser dbName zipFileURL"
	System.exit(-1)
}


def currActionUser = args[0]
def currActionDbName = args[1]
def importedFileUrl = args[2]

def currDebugMsg = "Invoking import: ${importedFileUrl}"

def builder = new AntBuilder()
	
def localZip="${context.serviceDirectory}/localZip.zip" 
   
builder.sequential {	  
	echo(message:"mysql_import.groovy: import: Getting ${importedFileUrl} to ${localZip} ...")
	get(src:"${importedFileUrl}", dest:"${localZip}", skipexisting:false)
	echo(message:"mysql_import.groovy: import: Unzipping ${localZip} to ${context.serviceDirectory} ...")
	unzip(src:"${localZip}", dest:"${context.serviceDirectory}", overwrite:true)	 
}

def fullPathToImport

def currZipFile = new java.util.zip.ZipFile(new File("${localZip}")) 
currZipFile.entries().each { entry ->  
	fullPathToImport = "${context.serviceDirectory}/${entry}"
	println "mysql_import.groovy: import: fullPathToImport is ${fullPathToImport}"
} 


importMysqlDB(binFolder,osConfig.mysqlProgram,currOsName,fullPathToImport as String,currActionDbName,currActionUser,currDebugMsg as String,"importOutput",true)
							
println "mysql_import.groovy: End"
	

