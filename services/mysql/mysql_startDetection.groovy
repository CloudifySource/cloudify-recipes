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
import groovy.sql.*
import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.dsl.utils.ServiceUtils;
import com.mysql.jdbc.Driver
import static mysql_runner.*

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())

context = ServiceContextFactory.getServiceContext()

def checkMasterStatus(context,config) {
	println "mysql_startDetection: checkMasterStatus: I am master ..."
	osConfig=ServiceUtils.isWindows() ? config.win64 : config.linux
	binFolder=context.attributes.thisInstance["binFolder"]	
	def currQuery = "\"" + "show master status;" + "\""
	def currDebugMsg = "Invoking query: ${currQuery}"
	      
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

	def masterStatus = showMasterStatus(context,binFolder,osConfig.mysqlProgram,currOsName,currQuery,config.dbName,"root",currDebugMsg,"binLogData",true)
	if (masterStatus) {
		println "mysql_startDetection: checkMasterStatus: master is ready "		
		return true
	}
	else {
		println "mysql_startDetection: checkMasterStatus: master is NOT ready yet..."		
		return false
	}
}

println "mysql_startDetection.groovy: jdbcPort is ${config.jdbcPort} ..."
if ( ServiceUtils.isPortOccupied(config.jdbcPort) ) { 				
	println "mysql_startDetection: port ${config.jdbcPort} is now occupied ..."	
	if ( config.startDetectionQuery.length() == 0  ) {
		println "mysql_startDetection: startDetectionQuery is empty - service is up "
		System.exit(0)
	}
	try {	
		def mysqlHost
		if (  context.isLocalCloud()  ) {
			mysqlHost =InetAddress.getLocalHost().getHostAddress()
		}
		else {
			mysqlHost =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
		}	
		def connUrl="jdbc:mysql://${mysqlHost}:${config.jdbcPort}/${config.dbName}"
		println "mysql_startDetection: connUrl is ${connUrl}"
		def sql = Sql.newInstance("${connUrl}", "${config.dbUser}","${config.dbPassW}", "com.mysql.jdbc.Driver")
		println "mysql_startDetection: ran query ${config.startDetectionQuery}"
		rows = sql.rows(config.startDetectionQuery)
		println "mysql_startDetection: rows value is ${rows}"
		if ( rows.size > 0 ) {
			println "mysql_startDetection: Rows size " + rows.size
			def row0 = rows[0]
			println "mysql_startDetection: row0 is ${row0}"
			if ( row0 != null ) {
				def cc = row0["cc"] as int
				context.attributes.thisInstance["cc"] = cc
				println "mysql_startDetection: Count " + cc				
				if ( cc > 0 ) {
					if ( config.masterSlaveMode ) {
						println "mysql_startDetection: in master-slave Mode ..."
						def isMaster = context.attributes.thisInstance["isMaster"]
						println "mysql_startDetection: isMaster is ${isMaster}..."
						if ( isMaster ) {											
							if (checkMasterStatus(context,config)) {
								println "mysql_startDetection: Master is ready"
								context.attributes.thisService["masterIsReady"]=true
								System.exit(0)
							}
							else {
								println "mysql_startDetection: Master is NOT ready yet..."
								System.exit(-1)							
							}
						}
					}
					else {
						println "mysql_startDetection: in standalone mode ..."
					}
					System.exit(0)
				}				
				else {
					System.exit(-1);
				}
			}
			println "mysql_startDetection: row0 is null"
		}	
		println "mysql_startDetection: rows.size is zero"	
		
	}
	catch (Exception e) {
		//println e.getMessage()
		System.exit(-1);
	} 
		
}
//return false;
System.exit(-1);