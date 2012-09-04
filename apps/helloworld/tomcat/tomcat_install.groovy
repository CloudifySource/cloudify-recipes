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

def config = new ConfigSlurper().parse(new File("tomcat-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()
def instanceID = context.getInstanceId()

println "tomcat_install.groovy: Installing tomcat..."

def home = "${context.serviceDirectory}/${config.name}"
def script = "${home}/bin/catalina"

context.attributes.thisInstance["home"] = "${home}"
context.attributes.thisInstance["script"] = "${script}"
println "tomcat_install.groovy: tomcat(${instanceID}) home is ${home}"

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
applicationWar = "${context.serviceDirectory}/${config.warName}"

origZipName="${context.serviceDirectory}/${config.zipName}"
zip2bInstalled="${installDir}/${config.zipName}"

//download apache tomcat
new AntBuilder().sequential {	
	mkdir(dir:"${installDir}")
	echo(message:"Copying ${origZipName} to ${zip2bInstalled} ...")
	copy(tofile: "${zip2bInstalled}", file:"${origZipName}", overwrite:true)  	
	
	echo(message:"Unzipping ${zip2bInstalled} to ${installDir} ...")
	unzip(src: "${zip2bInstalled}", dest:"${installDir}", overwrite:true)
	
	echo(message:"Moving${installDir}/${config.name} to ${home} ...")
	move(file:"${installDir}/${config.name}", tofile:"${home}")	
	chmod(dir:"${home}/bin", perm:'+x', includes:"*.sh")
}


new AntBuilder().sequential {			
	echo(message:"Deploying ${applicationWar} to ${home}/webapps ...")
	copy(tofile: "${home}/webapps/${config.warName}", file:"${applicationWar}", overwrite:true)
}


portIncrement = 0
if (context.isLocalCloud()) {
  portIncrement = instanceID - 1
  println "tomcat_install.groovy: Replacing default tomcat port with port ${config.port + portIncrement}"
}

def serverXmlFile = new File("${home}/conf/server.xml") 
def serverXmlText = serverXmlFile.text	
portReplacementStr = "port=\"${config.port + portIncrement}\""
ajpPortReplacementStr = "port=\"${config.ajpPort + portIncrement}\""
shutdownPortReplacementStr = "port=\"${config.shutdownPort + portIncrement}\""
serverXmlText = serverXmlText.replace("port=\"8080\"", portReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"8009\"", ajpPortReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"8005\"", shutdownPortReplacementStr) 
serverXmlText = serverXmlText.replace('unpackWARs="true"', 'unpackWARs="false"')
serverXmlFile.write(serverXmlText)


println "tomcat_install.groovy: Tomcat installation ended"
