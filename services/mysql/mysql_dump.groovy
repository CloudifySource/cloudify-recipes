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
import org.cloudifysource.dsl.context.ServiceContextFactory
import static mysql_runner.*


/* 
   This file enables users to create a database snapshot (mysqldump) 
    and to upload the backup to an external storage (Amazon S3 for example).
   Usage :  invoke mysql mysqldump actionUser dumpPrefix dbName backupType bucketName
   Example: invoke mysql mysqldump root myPrefix_ myDbName s3 myBucketName
   actionUser ( usually root ) = args[0]
   dumpPrefix = args[1]
   dbName = args[2]
   backupType = args[3]
   bucketName = args[4]
   
*/

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=ServiceUtils.isWindows() ? config.win64 : config.linux
context = ServiceContextFactory.getServiceContext()
mysqlHost=context.attributes.thisInstance["dbHost"]
binFolder=context.attributes.thisInstance["binFolder"]
println "mysql_dump.groovy: mysqlHost is ${mysqlHost} "

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
	println "mysql_dump.groovy: mysqldump error: Missing parameters\nUsage: invoke mysql mysqldump actionUser dumpPrefix dbName [backupType] [bucketName or folderName]"
	System.exit(-1)
}


def currActionUser = args[0]
def currDumpPrefix = args[1]
def currActionDbName
def currDebugMsg

currActionDbName = args[2]
currDebugMsg = "Invoking mysqldump on db ${currActionDbName} in ${mysqlHost} ..."	

def requiredBackupType = ""
def bucketName = ""
def backupFile = ""
if (args.length > 4) {
	def tmpRequiredBackupType = args[3]
	def tmpBucketName = args[4]
	
	if ("enableBackup" in config) {
		enableBackup = "${config.enableBackup}"
		if ( enableBackup ) {
			if ("backupType" in config) {
				def backupType = "${config.backupType}"
				println "mysql_dump.groovy: backupType is ${backupType}"	
	
				if ( backupType == "s3" && tmpRequiredBackupType == backupType) {
					requiredBackupType = tmpRequiredBackupType
					bucketName = tmpBucketName
					backupFile = "${context.serviceDirectory}/s3Put.sh"
				}
			}
		}
	}
}


def currActionArgs = "--add-drop-database -c --lock-all-tables -F"

def dumpFolder = System.properties["user.home"]
def s3Folder = "${config.s3Folder}"
runMysqlDump(binFolder,osConfig.mysqldump,currOsName,currActionArgs,currActionDbName,currActionUser,currDebugMsg,dumpFolder,currDumpPrefix,requiredBackupType,bucketName,backupFile,s3Folder)							
println "mysql_dump.groovy: End"
	

