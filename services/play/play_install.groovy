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

println "play_install.groovy: Installing..."

def config = new ConfigSlurper().parse(new File("play-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def home = System.getProperty("user.dir")+ "/${config.name}"
println "play_install.groovy: home is ${home}"
installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
println "play_install.groovy: installDir is ${installDir}"

new AntBuilder().sequential {	
	mkdir(dir:"${installDir}")
	get(src:"${config.downloadPath}", dest:"${installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${installDir}/${config.zipName}", dest:"${installDir}", overwrite:true)
	move(file:"${installDir}/${config.name}", tofile:"${home}")
	echo(message:"play_install.groovy: Chmodding +x ${home}/play")	
	chmod(dir:"${home}", perm:'+x', includes:"**/*")	
}




if ( !(config.applicationZipName) ||  "${config.applicationZipName}"=="OVERRIDE This property") {
		println "play_install.groovy: Not deploying any application"
}
else {
	applicationZip = "${installDir}/${config.applicationZipName}"
	if ("${config.applicationUrl}" != "" ) {    
		new AntBuilder().sequential {	
			echo(message:"play_install.groovy: Getting ${config.applicationUrl} to ${applicationZip} ...")
			get(src:"${config.applicationUrl}", dest:"${applicationZip}", skipexisting:false)
			unzip(src:"${applicationZip}", dest:"${installDir}", overwrite:true)
			echo(message:"play_install.groovy: Copying ${config.applicationName} to ${home}/playApps ...")
			move(file:"${installDir}/${config.applicationName}", tofile:"${home}/playApps/${config.applicationName}")		
		}
	}
}