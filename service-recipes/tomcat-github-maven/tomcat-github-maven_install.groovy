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

def config = new ConfigSlurper().parse(new File("tomcat-github-maven-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()

println "Installing tomcat-github-maven..."

def home = "${serviceContext.serviceDirectory}/${config.name}"
def script = "${home}/bin/catalina"

serviceContext.attributes.thisInstance["home"] = "${home}"
serviceContext.attributes.thisInstance["script"] = "${script}"
println "tomcat(${instanceID}) home is ${home}"

userHomeDir = System.properties["user.home"]
installDir = "${userHomeDir}/.cloudify/${config.serviceName}" + instanceID
applicationWar = "${installDir}/${config.warName}"

new AntBuilder().sequential {	
	mkdir(dir:"${installDir}")
	get(src:"${config.downloadPath}", dest:"${installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${installDir}/${config.zipName}", dest:"${home}", overwrite:true)
	move(file:"${home}/${config.name}", tofile:"${home}")
	chmod(dir:"${home}/bin", perm:'+x', includes:"*.sh")
}

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1
  println "Replacing default tomcat port with port ${config.port + portIncrement}"
}

def serverXmlFile = new File("${home}/conf/server.xml") 
def serverXmlText = serverXmlFile.text	
portReplacementStr = "port=\"${config.port + portIncrement}\""
ajpPortReplacementStr = "port=\"${config.ajpPort + portIncrement}\""
shutdownPortReplacementStr = "port=\"${config.shutdownPort + portIncrement}\""
serverXmlText = serverXmlText.replace("port=\"${config.port}\"", portReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"${config.ajpPort}\"", ajpPortReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"${config.shutdownPort}\"", shutdownPortReplacementStr) 
serverXmlText = serverXmlText.replace('unpackWARs="true"', 'unpackWARs="false"')
serverXmlFile.write(serverXmlText)

if (!serviceContext.isLocalCloud()) {
 new AntBuilder().sequential { 
  echo("copying ssh keys")
  mkdir(dir:"${userHomeDir}/.ssh")
  copy(todir: "${userHomeDir}/ssh", file:"id_rsa", overwrite:false)
  copy(todir: "${userHomeDir}/ssh", file:"id_rsa.pub", overwrite:false)
 }
}

new AntBuilder().sequential {
 echo("installing maven v${mavenVersion}")
 get(src:config.mavenDownloadUrl, dest:"${installDir}/${config.mavenZipFilename}", skipexisting:true)
 unzip(src:"${installDir}/${config.mavenZipFilename}", dest:"${home}", overwrite:true)
 move(file:"${home}/${config.mavenUnzipFolder}", tofile:"${serviceContext.serviceDirectory}")
 chmod(dir:"${home}/bin", perm:'+x', includes:"*.sh")
}

new AntBuilder().sequential {
 echo("downloading source code from ${applicationSrcUrl}")
 exec(executable:"git", dir:"${home}") {
  arg("clone")
  arg("${applicationSrcUrl}")
 }
 echo("building war file")
 exec(executable:"${installDir}/${config.mavenUnzipFolder}/bin/mvn", dir:"${applicationSrcFolder}") {
  arg(value:"clean")
  arg(value:"package")
 }
 echo("deploying war file")
 copy(todir: "${home}/webapps", file:"${home}/${applicationSrcFolder}/target/${config.applicationWarFilename}", overwrite:true)
}

println "Installation complete"

