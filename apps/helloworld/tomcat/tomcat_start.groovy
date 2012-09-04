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
import org.cloudifysource.dsl.utils.ServiceUtils;
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

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

currJmxPort=config.jmxPort+portIncrement
println "tomcat_start.groovy: jmx port is ${currJmxPort}"

def scriptExt
def osFamilyName

if ( ServiceUtils.isWindows() ){
	script +=".bat"
	osFamilyName="windows"
}
else {
	script +=".sh"
	osFamilyName="unix"	
}

println "tomcat_start.groovy: tomcat(${instanceID}) executing ${script} ..."

new AntBuilder().sequential {
	exec(executable:"${script}", osfamily:"${osFamilyName}") {
        env(key:"CATALINA_HOME", value: "${home}")
        env(key:"CATALINA_BASE", value: "${home}")
        env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")        
		arg(value:"run")
	}
}
