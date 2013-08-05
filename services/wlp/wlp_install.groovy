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
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

def config = new ConfigSlurper().parse(new File("wlp-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()
def instanceID = context.getInstanceId()

println "wlp_install.groovy: Installing websphere liberty..."

warUrl= context.attributes.thisService["warUrl"]
if ( warUrl == null ) {
warUrl = "${config.applicationWarUrl}"
}

def installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
def applicationWar = "${installDir}/${config.warName}.war"

//download WAS liberty
new AntBuilder().sequential {	
mkdir(dir:"${installDir}")

if ( config.downloadPath.toLowerCase().startsWith("http") || config.downloadPath.toLowerCase().startsWith("ftp")) {
echo(message:"Getting ${config.downloadPath} to ${installDir}/${config.jarName} ...")
exec(executable:"wget", dir:"${installDir}", osfamily:"unix") {
                arg(value:"${config.downloadPath}")                              
               }
}	
else {
echo(message:"Copying ${context.serviceDirectory}/${config.downloadPath} to ${installDir}/${config.jarName} ...")	
copy(tofile: "${installDir}/${config.jarName}", file:"${context.serviceDirectory}/${config.downloadPath}", overwrite:false)
}
}

if ( warUrl != null && "${warUrl}" != "" ) {
new AntBuilder().sequential {
if ( warUrl.toLowerCase().startsWith("http") || warUrl.toLowerCase().startsWith("ftp")) {
echo(message:"Getting ${warUrl} to ${applicationWar} ...")
//get(src:"${warUrl}", dest:"${installDir}", skipexisting:false)
exec(executable:"wget", dir:"${installDir}", osfamily:"unix") {
                arg(value:"${warUrl}")                              
               }
}	
else {
echo(message:"Copying ${context.serviceDirectory}/${warUrl} to ${applicationWar} ...")	
copy(tofile: "${applicationWar}", file:"${context.serviceDirectory}/${warUrl}", overwrite:true)	
}
}
}

def cmdStr="java -jar ${installDir}/${config.jarName} --acceptLicense < ${installDir}"
println "b4 cmdStr : ${cmdStr} ... "
def process="${cmdStr}".execute()
process.in.eachLine {line->
    println "output " + line
}

println "wlp_install.groovy: wlp installation ended"
