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
import java.util.concurrent.TimeUnit
import org.cloudifysource.dsl.utils.ServiceUtils;

jbossMySqlConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())

println "jboss_start.groovy: Calculating mysqlServiceHost..."
serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()
println "jboss_start.groovy: This jboss instance ID is ${instanceID}"

def dbServiceHost

if ( "${jbossMySqlConfig.dbServiceName}"!="NO_DB_REQUIRED" ) {
	println "jboss_start.groovy: waiting for ${jbossMySqlConfig.dbServiceName}..."
	def dbService = serviceContext.waitForService(jbossMySqlConfig.dbServiceName, 20, TimeUnit.SECONDS) 
	def dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 
	dbServiceHost = dbInstances[0].hostAddress
	println "jboss_start.groovy: ${jbossMySqlConfig.dbServiceName} host is ${dbServiceHost}"		
}
else {
	dbServiceHost="DUMMY_HOST"
}	

def driverConnectorString =  "</datasource><datasource jndi-name=\"java:jboss/exported/MySqlDS\" pool-name=\"MySqlDS\" enabled=\"true\"><connection-url>jdbc:mysql://${dbServiceHost}:${jbossMySqlConfig.dbPortVarName}/${jbossMySqlConfig.dbName}</connection-url><driver>${jbossMySqlConfig.jdbcDriverName}</driver><security><user-name>${jbossMySqlConfig.dbUser}</user-name><password>${jbossMySqlConfig.dbPassW}</password></security></datasource>"

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

new AntBuilder().sequential {	
 get(src:"${jbossMySqlConfig.jdbcDriverUrl}", dest:"${jbossMySqlConfig.applicationWarFolder}", skipexisting:true)
}

serverXmlFile = new File("${jbossMySqlConfig.standaloneXmlFile}") 
serverXmlText = serverXmlFile.text	
serverXmlFile.text = serverXmlText.replace("</datasource>",driverConnectorString)

script = "${jbossMySqlConfig.home}/bin/standalone"

println "jboss_start.groovy executing ${script} ..."
new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix") {}
}

println "jboss_start.groovy End of ${script}"







