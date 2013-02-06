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

println "couchbase_stop.groovy: stoping ..."

context = ServiceContextFactory.getServiceContext()
def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
def currVendor=context.attributes.thisInstance["osType"]
def stopScript
switch (currVendor) {
	case ["Ubuntu"]:			
		stopScript="${scriptsFolder}/stop.sh"
		break	
	case ["CentOS"]:			
		stopScript="${scriptsFolder}/stop.sh"
		break	
	case ["Windows"]:
		stopScript="${scriptsFolder}/stop.bat"
		break
	default: 
		stopScript="${scriptsFolder}/stop.sh"
		break	
}

builder = new AntBuilder()
builder.sequential {
	echo(message:"couchbase_stop.groovy: Running ${stopScript} ...")
	exec(executable:"${stopScript}", failonerror: "true")        	
}


println "couchbase_stop.groovy: End of stop script"