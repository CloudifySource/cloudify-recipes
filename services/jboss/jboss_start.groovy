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
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import org.cloudifysource.dsl.utils.ServiceUtils;

jbossConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())

println "jboss_start.groovy: Calculating DBServiceHost..."
serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()
println "jboss_start.groovy: This jboss instance ID is ${instanceID}"

def dbServiceHost
def dbServicePort

if ( "${jbossConfig.dbServiceName}"!="NO_DB_REQUIRED" ) {
	println "jboss_start.groovy: waiting for ${jbossConfig.dbServiceName}..."
	def dbService = serviceContext.waitForService(jbossConfig.dbServiceName, 20, TimeUnit.SECONDS) 
	def dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 
	dbServiceHost = dbInstances[0].hostAddress
	println "jboss_start.groovy: ${jbossConfig.dbServiceName} host is ${dbServiceHost}"		
	def dbServiceInstances = serviceContext.attributes[jbossConfig.dbServiceName].instances
	dbServicePort = dbServiceInstances[1].port
	println "jboss_start.groovy: ${jbossConfig.dbServiceName} port is ${dbServicePort}"		
}
else {
	dbServiceHost="DUMMY_HOST"
	dbServicePort="DUMMY_PORT"
}	

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

script = "${jbossConfig.home}/bin/standalone"

if(ServiceUtils.isWindows()) {
	println "jboss_start.groovy: Adding DB port and host to ${script} .bat ..."
	searchStr="set JAVA_OPTS="
	standAloneFile = new File("${script}.bat") 
	standAloneText = standAloneFile.text	
	replaceStr = "set DB_HOST=${dbServiceHost}"+"\n"
	replaceStr = replaceStr + "set DB_PORT=${dbServicePort}" +"\n"
	replaceStr = replaceStr + "set JAVA_OPTS=-DDB_PORT=%DB_PORT% -DB_HOST=%DB_HOST% %JAVA_OPTS%"+"\n"
	replaceStr = replaceStr + searchStr
	standAloneFile.text = standAloneText.replace(searchStr, replaceStr) 
}
else {
	println "jboss_start.groovy: Adding DB port and host to ${script} .sh ..."
	searchStr="DIRNAME="
	standAloneFile = new File("${script}.sh") 
	standAloneText = standAloneFile.text	
	replaceStr = "DB_HOST=${dbServiceHost}"+"\n"
	replaceStr = replaceStr + "DB_PORT=${dbServicePort}" +"\n"
	replaceStr = replaceStr + "JAVA_OPTS=\"-DDB_PORT=\$DB_PORT -DDB_HOST=\$DB_HOST -XX:MaxPermSize=512M \$JAVA_OPTS\""+"\n"
	replaceStr = replaceStr + searchStr
	standAloneFile.text = standAloneText.replace(searchStr, replaceStr) 
}



println "jboss_start.groovy executing ${script} ..."
new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix") {        
		env(key:"DB_HOST", value: "${dbServiceHost}")
        env(key:"DB_PORT", value: "${dbServicePort}")
	}
	exec(executable:"${script}.bat", osfamily:"windows") {       
		env(key:"DB_HOST", value: "${dbServiceHost}")
        env(key:"DB_PORT", value: "${dbServicePort}")
	}
}

println "jboss_start.groovy End of ${script}"







