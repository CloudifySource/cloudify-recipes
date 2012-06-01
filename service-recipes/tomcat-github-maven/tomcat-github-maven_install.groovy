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

def privateKeyFilename="id_rsa"
def privateKeySourceFolder=".ssh"
def privateKeyTargetFolder="${userHomeDir}/.ssh"
def privateKeyInstalled = new File("${privateKeyTargetFolder}/${privateKeyFilename}").exists();
if (!privateKeyInstalled) {

 if (!new File("${privateKeySourceFolder}/${privateKeyFilename}").exists()) {
  throw new java.io.FileNotFoundException("place add a private key to the recipe for accessing github: ${privateKeySourceFolder}/${privateKeyFilename}");
 }
 
 new AntBuilder().sequential { 
  echo("installing private ssh key")
  mkdir(dir:"${userHomeDir}/.ssh")
  copy(todir: privateKeyTargetFolder, overwrite:true) {
	fileset(dir: privateKeySourceFolder)
  }
  
  chmod(dir: privateKeyTargetFolder, perm:"600", includes:"**/*")
 }
}

new AntBuilder().sequential {
 echo("installing maven v${config.mavenVersion}")
 get(src:config.mavenDownloadUrl, dest:"${installDir}/${config.mavenZipFilename}", skipexisting:true)
 unzip(src:"${installDir}/${config.mavenZipFilename}", dest:"${home}", overwrite:true)
 chmod(dir:"${home}/${config.mavenUnzipFolder}/bin", perm:'+x', excludes:"*.bat")
}

def mvn="${home}/${config.mavenUnzipFolder}/bin/mvn"

new AntBuilder().sequential {
 echo("downloading source code from ${config.applicationSrcUrl}")
 exec(executable:"git") {
  arg(value:"clone")
  arg(value:"-q")
  arg(value:"-v")
  arg(value:"${config.applicationSrcUrl}")
  arg(value:"${home}/${config.applicationSrcFolder}")
 }
 echo("building war file")
 exec(executable:mvn, dir:"${home}/${config.applicationSrcFolder}") {
  arg(value:"clean")
  arg(value:"package")
 }
 echo("deploying war file")
 copy(todir: "${home}/webapps", file:"${home}/${config.applicationSrcFolder}/target/${config.applicationWarFilename}", overwrite:true)
}

println "Installation complete"

