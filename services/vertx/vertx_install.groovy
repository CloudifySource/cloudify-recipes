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
import static TemplateGenerator.*
import java.util.concurrent.TimeUnit

println "vertx_install.groovy: Installing..."

def config = new ConfigSlurper().parse(new File("vertx-service.properties").toURL())
def context = ServiceContextFactory.serviceContext
def instanceID = context.instanceId
def home = "${context.serviceDirectory}/${config.name}"
println "vertx.groovy: home is ${home}"
def javaHome = "${context.serviceDirectory}/jdk${config.javaVersion}"
println "vertx.groovy: javaHome is ${javaHome}"
def installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
println "vertx_install.groovy: installDir is ${installDir}"

context.attributes.thisInstance["home"] = home as String 
context.attributes.thisInstance["installDir"] = installDir as String
context.attributes.thisInstance["javaHome"] = javaHome as String


//installing vertx
def builder = new AntBuilder()
builder.sequential {
	mkdir(dir:"${installDir}")
	get(src:"${config.downloadPath}", dest:"${installDir}/${config.zipName}", skipexisting:true)
    //todo - windows support
	get(src:"${config.javaUrl}", dest:"${installDir}/java7.tar.gz", skipexisting:true)
    untar(src:"${installDir}/${config.zipName}", dest:"${installDir}", compression:"gzip", overwrite:true)
    move(file:"${installDir}/${config.name}", tofile:"${home}")
    untar(src:"${installDir}/java7.tar.gz", dest:"${context.serviceDirectory}", compression:"gzip", overwrite:true)
    chmod(dir:"${home}/bin", perm:'+x', includes:"**/*")
	chmod(dir:"${javaHome}/bin", perm:'+x', includes:"**/*")
}

//set cluster properties if needed
if (config.cluster) {
    service = context.waitForService(context.serviceName, 20, TimeUnit.SECONDS)
    instances = service?.waitForInstances(service?.numberOfPlannedInstances, 20, TimeUnit.SECONDS)
    config.clusterConfig["hosts"] = []
    instances?.each {
        config.clusterConfig["hosts"] << it.hostAddress
    }
}

if (config.installJRuby) {
    builder.sequential {
        def jrubyHome = "${context.serviceDirectory}/jruby-${config.jrubyVersion}"
        get(src:"${config.jrubyUrl}", dest:"${installDir}/jruby.zip", skipexisting:true)
        unzip(src:"${installDir}/jruby.zip", dest:"${context.serviceDirectory}", overwrite:true)
        chmod(dir:"${jrubyHome}/bin", perm:'+x', includes:"**/*")
        exec(executable:"${jrubyHome}/bin/jruby", dir:"${jrubyHome}/bin", osfamily:"unix") {
            arg(line:"-S gem install json")
            env(key:"JAVA_HOME", value:javaHome)
            env(key:"JRUBY_HOME", value:jrubyHome)
        }
        context.attributes.thisInstance["jrubyHome"] = "${jrubyHome}"
    }
}


if (config.installJython) {
    def jythonHome =  "${context.serviceDirectory}/jython-${config.jythonVersion}"
    builder.sequential {
        get(src:"${config.jythonUrl}", dest:"${installDir}/jython.jar", skipexisting:true)
        java(fork:true, failonerror: true, jvm:"${javaHome}/bin/java", jar: "${installDir}/jython.jar") {
            arg(line: "-s -d \"${jythonHome}\"")
        }
        context.attributes.thisInstance["jythonHome"] = "${jythonHome}"
    }
}

generateTemplates("${context.serviceDirectory}/templates", "${home}" , config.clusterConfig)

if (config.runMode == "examples") {
	entryPointFullPath = "${home}/examples/${config.entryPoint}"
	applicationDir = entryPointFullPath.substring(0, entryPointFullPath.lastIndexOf("/"))
	entryPoint = entryPointFullPath.substring(entryPointFullPath.lastIndexOf("/")+1) 
	context.attributes.thisInstance["entryPoint"] = entryPoint as String
   	context.attributes.thisInstance["applicationDir"] = applicationDir as String 
} else if (config.runMode == "application") {
	applicationZip = "${installDir}/${config.applicationZipName}"
	applicationDir = "${home}/apps/${config.applicationName}"
	println "vertx_install.groovy: Getting ${config.applicationZipUrl} to ${applicationZip}..."
    builder.sequential {
        get(src:"${config.applicationZipUrl}", dest:"${applicationZip}", skipexisting:true)
        mkdir(dir:"${applicationDir}")
        unzip(src:"${applicationZip}", dest:"${applicationDir}", overwrite:true)
    }
   	context.attributes.thisInstance["entryPoint"] = entryPoint as String
   	context.attributes.thisInstance["applicationDir"] = applicationDir as String 
} else {//module
    context.attributes.thisInstance["entryPoint"] = config.entryPoint
    context.attributes.thisInstance["applicationDir"] = ${home} as String
}


