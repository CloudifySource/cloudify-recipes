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

config = new ConfigSlurper().parse(new File('mongoConfig-service.properties').toURL())

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

currPort = serviceContext.attributes.thisInstance["port"]
println "mongoConfig_start.groovy: mongoConfig#${instanceID} is using port ${currPort}"

home = serviceContext.attributes.thisInstance["home"]
println "mongoConfig_start.groovy: home ${home}"

script= serviceContext.attributes.thisInstance["script"]
println "mongoConfig_start.groovy: script ${script}"


dataDir = "${home}/data/cfg"
println "mongoConfig_start.groovy: dataDir is ${dataDir}"

println "mongoConfig_start.groovy: Running script ${script} for mongoConfig#${instanceID}..."
new AntBuilder().sequential {
	//creating the data directory 	
    
	mkdir(dir:dataDir)
	exec(executable:"${script}") {
		arg line:"--journal"
		arg value:"--configsvr"
		arg line:"--dbpath \"${dataDir}\""
		arg line:"--port ${currPort}"
    }
}

println "mongoConfig_start.groovy: Script ${script} ended for mongoConfig#${instanceID}"
