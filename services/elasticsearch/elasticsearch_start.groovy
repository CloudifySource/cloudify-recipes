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


context = ServiceContextFactory.getServiceContext()

def instanceID = context.instanceId
println "elasticsearch_start.groovy: instanceID is ${instanceID}"

println "elasticsearch_start.groovy: host address is " + context.attributes.thisInstance["myHostAddress"]

def installationFolder = context.attributes.thisInstance["installationFolder"]
println "elasticsearch_start.groovy: installationFolder is ${installationFolder}"

def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
println "elasticsearch_start.groovy: scriptsFolder is ${scriptsFolder}"

builder = new AntBuilder()

def runScript = "run.sh"
builder.sequential {
	echo(message: "elasticsearch_start.groovy: Running ${scriptsFolder}/${runScript}")
	exec(executable: "${scriptsFolder}/${runScript}",failonerror: "true") {		
		arg(value:installationFolder)		
	}
}

println "elasticsearch_start.groovy: End of start"