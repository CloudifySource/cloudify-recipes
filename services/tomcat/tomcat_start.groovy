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
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def config=new ConfigSlurper().parse(new File("tomcat-service.properties").toURL())

println "tomcat_start.groovy: Calculating mongoServiceHost..."
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
println "tomcat_start.groovy: This tomcat instance ID is ${instanceID}"

def home= serviceContext.attributes.thisInstance["home"]
println "tomcat_start.groovy: tomcat(${instanceID}) home ${home}"

def script= serviceContext.attributes.thisInstance["script"]
println "tomcat_start.groovy: tomcat(${instanceID}) script ${script}"

if ( !(config.dbServiceName) ||  "${config.dbServiceName}"=="NO_DB_REQUIRED") {
	println "Using dummy db host(DUMMY_HOST) and port(0)"
	dbServiceHost="DUMMY_HOST"
	dbServicePort="0"
}
else {
	println "tomcat_start.groovy: waiting for ${config.dbServiceName}..."
	def dbService = serviceContext.waitForService(config.dbServiceName, 20, TimeUnit.SECONDS) 
	def dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 
    dbServiceHost = dbInstances[0].hostAddress
	println "tomcat_start.groovy: ${config.dbServiceName} host is ${dbServiceHost}"
	def dbServiceInstances = serviceContext.attributes[config.dbServiceName].instances
	dbServicePort = dbServiceInstances[1].port
	println "tomcat_start.groovy: ${config.dbServiceName} port is ${dbServicePort}"
}


println "tomcat_start.groovy executing ${script}"

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

currJmxPort=config.jmxPort+portIncrement
println "tomcat_start.groovy: jmx port is ${currJmxPort}"

javaOpts = config.javaOpts

// in case this property is not defined, groovy defaults to an empty Map. so we need to convert back to an empty string
if (! (javaOpts instanceof String) ) {
	javaOpts = ""
}

println "tomcat_start.groovy: Additional java opts are ${javaOpts}"

new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix") {
        env(key:"CATALINA_HOME", value: "${home}")
        env(key:"CATALINA_BASE", value: "${home}")
        env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false ${javaOpts}")
        env(key:"${config.dbHostVarName}", value: "${dbServiceHost}")
        env(key:"${config.dbPortVarName}", value: "${dbServicePort}")		
		arg(value:"run")
	}
	exec(executable:"${script}.bat", osfamily:"windows") { 
        env(key:"CATALINA_HOME", value: "${home}")
        env(key:"CATALINA_BASE", value: "${home}")
        env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false ${javaOpts}")
        env(key:"${config.dbHostVarName}", value: "${dbServiceHost}")
        env(key:"${config.dbPortVarName}", value: "${dbServicePort}")	
		arg(value:"run")
	}
}
