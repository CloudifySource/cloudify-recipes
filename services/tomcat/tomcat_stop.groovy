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
println "tomcat_stop.groovy: About to stop tomcat..."

def serviceContext = ServiceContextFactory.getServiceContext()

def instanceID=serviceContext.instanceId
def home= serviceContext.attributes.thisInstance["home"]
println "tomcat_stop.groovy: tomcat(${instanceID}) home ${home}"

def script= serviceContext.attributes.thisInstance["script"]
if (script) {
println "tomcat_stop.groovy: tomcat(${instanceID}) script ${script}"


println "tomcat_stop.groovy: executing command ${script}"
new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix") {
        env(key:"CATALINA_HOME", value: "${home}")
    env(key:"CATALINA_BASE", value: "${home}")
    env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		arg(value:"stop")
	}
	exec(executable:"${script}.bat", osfamily:"windows"){
        env(key:"CATALINA_HOME", value: "${home}")
    env(key:"CATALINA_BASE", value: "${home}")
    env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		arg(value:"stop")
	}
}

println "tomcat_stop.groovy: tomcat is stopped"
}