/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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

println "tomcat_start.groovy: Starting Tomcat"

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/tomcat-service.properties").toURL())
def instanceId = context.instanceId
println "tomcat_start.groovy: This tomcat instance Id is ${instanceId}"

def catalinaHome = context.attributes.thisInstance["catalinaHome"]
println "tomcat_start.groovy: tomcat(${instanceId}) catalinaHome ${catalinaHome}"
def catalinaBase = context.attributes.thisInstance["catalinaBase"]
println "tomcat_start.groovy: tomcat(${instanceId}) catalinaBase ${catalinaBase}"
def catalinaOpts = context.attributes.thisInstance["catalinaOpts"]
println "tomcat_start.groovy: tomcat(${instanceId}) catalinaOpts ${catalinaOpts}"
def javaOpts = context.attributes.thisInstance["javaOpts"]
println "tomcat_start.groovy: tomcat(${instanceId}) javaOpts ${javaOpts}"
def envVar = context.attributes.thisInstance["envVar"]

if ( config.dbServiceName &&  "${config.dbServiceName}" != "NO_DB_REQUIRED") {
	println "tomcat_start.groovy: waiting for ${config.dbServiceName}..."
	def dbService = context.waitForService(config.dbServiceName, 20, TimeUnit.SECONDS)
	def dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 
	def dbServiceHost = dbInstances[0].hostAddress
	envVar.put(config.dbHostVarName, "${dbServiceHost}")
	println "tomcat_start.groovy: ${config.dbServiceName} host is ${dbServiceHost}"
	def dbServiceInstances = context.attributes[config.dbServiceName].instances
	dbServicePort = dbServiceInstances[1].port
	envVar.put(config.dbPortVarName, "${dbServicePort}")
	println "tomcat_start.groovy: ${config.dbServiceName} port is ${dbServicePort}"
}
println "tomcat_start.groovy: tomcat(${instanceId}) envVar ${envVar}"


// trick to be able to havee several instances with localcloud deployment
portIncrement = 0
if (context.isLocalCloud()) {
	portIncrement = instanceId - 1  
}

currJmxPort=config.jmxPort+portIncrement
println "tomcat_start.groovy: jmx port is ${currJmxPort}"

new AntBuilder().sequential {
	exec(executable:"${catalinaHome}/bin/catalina.sh", osfamily:"unix") {
		env(key:"CLASSPATH", value: "") // reset CP to avoid side effects (Cloudify passes all the required files to Groovy in the classpath)
		envVar.each{
			env(key:it.key, value:it.value)
		}
		env(key:"CATALINA_HOME", value: "${catalinaHome}")
		env(key:"CATALINA_BASE", value: "${catalinaBase}")
		env(key:"CATALINA_OPTS", value: "${catalinaOpts} -Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
		env(key:"JAVA_OPTS", value: "${javaOpts}")
		arg(value:"run")
	}
	exec(executable:"${catalinaHome}/bin/catalina.bat", osfamily:"windows") { 
		env(key:"CLASSPATH", value: "") // reset CP to avoid side effects (Cloudify passes all the required files to Groovy in the classpath)
		envVar.each{
			env(key:it.key, value:it.value)
		}
		env(key:"CATALINA_HOME", value: "${catalinaHome}")
		env(key:"CATALINA_BASE", value: "${catalinaBase}")
		env(key:"CATALINA_OPTS", value: "${catalinaOpts} -Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
		env(key:"JAVA_OPTS", value: "${javaOpts}")
		arg(value:"run")
	}
}
