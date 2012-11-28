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

println "updateWarFile.groovy: Starting..."

context = ServiceContextFactory.getServiceContext()
config  = new ConfigSlurper().parse(new File("${context.serviceDirectory}/tomcat-service.properties").toURL())

def instanceID = context.getInstanceId()
installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
applicationWar = "${installDir}/${config.warName}"

newWarFile=context.attributes.thisService["warUrl"] 
println "updateWarFile.groovy: newWarFile is ${newWarFile}"

home = context.attributes.thisInstance["home"]
webApps="${home}/webapps"
destWarFile="${webApps}/${config.warName}"
println "updateWarFile.groovy: destWarFile is ${destWarFile}"

new AntBuilder().sequential {
	
	echo(message:"updateWarFile.groovy: Getting ${newWarFile} ...")
	get(src:"${newWarFile}", dest:"${applicationWar}", skipexisting:false)
	
	echo(message:"updateWarFile.groovy: Copying ${applicationWar} to ${webApps}...")
	copy(todir: "${webApps}", file:"${applicationWar}", overwrite:true)	
}

println "updateWarFile.groovy: End"
return true


