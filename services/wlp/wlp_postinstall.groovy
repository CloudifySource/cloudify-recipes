/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import org.cloudifysource.dsl.context.ServiceContextFactory

def config = new ConfigSlurper().parse(new File("wlp-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()
def instanceID = context.getInstanceId()
def installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
def applicationWar = "${installDir}/${config.warName}.war"
println "wlp_postinstall.groovy: Creating websphere liberty server..."

wlpDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID+"/wlp"
println "install dir is ${wlpDir} creating ${config.serverName}"
new AntBuilder().sequential {
exec(executable:"${wlpDir}/bin/server", osfamily:"unix") {
env(key:"JAVA_HOME", value: "${config.javaHome}")
arg(value:"create")
arg(value:"${config.serverName}")
}
}

println "Finish liberty installation ..."

new AntBuilder().sequential {
echo(message:"Create dir ${installDir}/wlp/usr/servers/{$config.serverName}/apps")
mkdir(dir:"${installDir}/wlp/usr/servers/${config.serverName}/apps")
echo(message:"Copying ${applicationWar} to ${installDir}/wlp/usr/servers/${config.serverName}/apps ...")
copy(tofile: "${installDir}/wlp/usr/servers/${config.serverName}/apps/${config.warName}.war", file:"${applicationWar}", overwrite:true)
}

new AntBuilder().sequential {
echo(message:"Getting the slf4 jars to be installed ...")
exec(executable:"wget", dir:"${installDir}", osfamily:"unix") {arg(value:"http://s3.amazonaws.com/aharon_wlp85/${config.jar1}")}
exec(executable:"wget", dir:"${installDir}", osfamily:"unix") {arg(value:"http://s3.amazonaws.com/aharon_wlp85/${config.jar2}")}
exec(executable:"wget", dir:"${installDir}", osfamily:"unix") {arg(value:"http://s3.amazonaws.com/aharon_wlp85/${config.jar3}")}
//get(src:"http://s3.amazonaws.com/aharon_wlp85/${config.jar1}", dest:"${installDir}", skipexisting:false)
//get(src:"http://s3.amazonaws.com/aharon_wlp85/${config.jar2}", dest:"${installDir}", skipexisting:false)
//get(src:"http://s3.amazonaws.com/aharon_wlp85/${config.jar3}", dest:"${installDir}", skipexisting:false)
echo(message:"Done the slf4 jars to be installed ...")
}

println "wlp_postinstall.groovy: update server.xml file"
installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
def Name=config.restAppName
def serverXmlFile = new File("${wlpDir}/usr/servers/${config.serverName}/server.xml")
def serverXmlText = serverXmlFile.text	
serverXmlText = serverXmlText.replace("localhost","0.0.0.0")
serverXmlText = serverXmlText.replace("</server>","<application context-root=\"${Name}\" id=\"${Name}\" location=\"${config.warName}.war\" name=\"${Name}\" type=\"war\"> <classloader delegation=\"parenLast\"> <privateLibrary name=\"emRestLib\"> <fileset dir=\"${installDir}\" includes=\"${config.jar1},${config.jar2},${config.jar3}\"/> </privateLibrary> </classloader> </application> </server>")
serverXmlFile.write(serverXmlText)

println "wlp_postinstall.groovy: wlp installation ended"
