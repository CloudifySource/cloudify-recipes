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
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

println "tomcat_start.groovy: Starting Tomcat"

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/tomcat-service.properties").toURL())
def instanceId = context.instanceId
println "tomcat_start.groovy: This tomcat instance Id is ${instanceId}"

def catalinaHome = context.attributes.thisInstance["catalinaHome"]
println "tomcat_start.groovy: tomcat(${instanceId}) catalinaHome ${catalinaHome}"

// trick to be able to have several instances with localcloud deployment
portIncrement = 0
if (context.isLocalCloud()) {
	portIncrement = instanceId - 1  
}

currJmxPort=config.jmxPort+portIncrement
println "tomcat_start.groovy: jmx port is ${currJmxPort}"

new AntBuilder().sequential {
	exec(executable:"${catalinaHome}/bin/catalina.sh", osfamily:"unix") {
		env(key:"CLASSPATH", value: "") // reset CP to avoid side effects (Cloudify passes all the required files to Groovy in the classpath)
		arg(value:"run")
	}
}
