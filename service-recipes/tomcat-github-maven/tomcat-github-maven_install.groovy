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
import org.cloudifysource.dsl.utils.ServiceUtils

def config = new ConfigSlurper().parse(new File("tomcat-github-maven-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()
def tomcatConfig=new ConfigSlurper().parse(new File("${context.serviceDirectory}/tomcat.properties").toURL())

def instanceID = context.getInstanceId()

println "Installing tomcat-github-maven..."

println "tomcatConfig.name=${tomcatConfig.name}"
def home = "${context.serviceDirectory}/${tomcatConfig.name}"
context.attributes.thisInstance["home"] = "${home}"
println "tomcat(${instanceID}) home is ${home}"

def script = "${home}/bin/catalina"
context.attributes.thisInstance["script"] = "${script}"

userHomeDir = System.properties["user.home"]
installDir = "${userHomeDir}/.cloudify/${context.serviceName}" + instanceID

def ant = new AntBuilder()
def git = new GitBuilder()
def mvn = new MavenBuilder()

ant.sequential {
	mkdir(dir:installDir)
	get(src:"${tomcatConfig.downloadPath}", dest:"${installDir}/${tomcatConfig.zipName}", skipexisting:true)
	unzip(src:"${installDir}/${tomcatConfig.zipName}", dest:"${context.serviceDirectory}", overwrite:true)
	chmod(dir:"${home}/bin", perm:'+x', includes:"*.sh")
}

portIncrement = 0
if (context.isLocalCloud()) {
  portIncrement = instanceID - 1
  println "Replacing default tomcat port with port ${tomcatConfig.port + portIncrement}"
}

def serverXmlFile = new File("${home}/conf/server.xml") 
def serverXmlText = serverXmlFile.text	
portReplacementStr = "port=\"${tomcatConfig.port + portIncrement}\""
ajpPortReplacementStr = "port=\"${tomcatConfig.ajpPort + portIncrement}\""
shutdownPortReplacementStr = "port=\"${tomcatConfig.shutdownPort + portIncrement}\""
serverXmlText = serverXmlText.replace("port=\"${tomcatConfig.port}\"", portReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"${tomcatConfig.ajpPort}\"", ajpPortReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"${tomcatConfig.shutdownPort}\"", shutdownPortReplacementStr) 
serverXmlText = serverXmlText.replace('unpackWARs="true"', 'unpackWARs="false"')
serverXmlFile.write(serverXmlText)

git.installGit()
mvn.installMaven()
ant.echo("downloading source code from ${config.applicationSrcUrl}")
git.clone(config.applicationSrcUrl,"${context.serviceDirectory}/${config.applicationSrcFolder}", verbose:true)

def pom = "${context.serviceDirectory}/${config.applicationSrcFolder}/pom.xml"
if (!(new File(pom).exists())) {
	throw new java.io.FileNotFoundException(pom + " does not exist");
}
println "Installation complete"

