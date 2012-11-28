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
	This file enables users to add a slave to the master.
	It should be invoked only on a master instance (by a remote slave) and only if masterSlaveMode is set to true on both the slave and master.
	As a result, the following will be invoked :  
	mysql -u root -D dbName -e "GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO slaveUser@'slaveHostIP' IDENTIFIED BY 'slavePassword';"
	
	Usage :  invoke mysqlmaster addSlave actionUser dbName slaveUser slavePassword slaveHostIP 
*/	

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=ServiceUtils.isWindows() ? config.win64 : config.linux
context = ServiceContextFactory.getServiceContext()



if ( !config.masterSlaveMode ) {
	println "mysql_addSlave.groovy: masterSlaveMode is disabled. I cannot invoke addSlave"
	System.exit(-1)
}

def isMaster = context.attributes.thisInstance["isMaster"]

if ( !isMaster ) {
	println "mysql_addSlave.groovy: I am not a master. I cannot invoke addSlave"
	System.exit(-1)
}

binFolder=context.attributes.thisInstance["binFolder"]
println "mysql_addSlave.groovy: binFolder is ${binFolder} "

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


if (args.length < 5) {
	println "mysql_addSlave.groovy: addSlave error: Missing parameters\nUsage: invoke mysqlmaster addSlave actionUser dbName slaveUser slavePassword slaveHostIP"
	System.exit(-1)
}

def currActionUser = args[0]
def currActionDbName = args[1]
def slaveUser = args[2]
def slavePassword = args[3]
def slaveHostIP = args[4]


def grantStr = "GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO ${slaveUser}@'${slaveHostIP}' IDENTIFIED BY '${slavePassword}';"
def currQuery = "\"" + grantStr + "\""
def currDebugMsg = "Invoking query: ${currQuery}"

println "mysql_addSlave.groovy: b4 invoking ${currQuery} ... " 
runMysqlQuery(binFolder,osConfig.mysqlProgram,currOsName,currQuery as String,currActionDbName as String,currActionUser as String,currDebugMsg as String,"addSlaveOutput",true)
							
println "mysql_addSlave.groovy: End"
	

