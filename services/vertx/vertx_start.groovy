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

def config = new ConfigSlurper().parse(new File("vertx-service.properties").toURL())
def context = ServiceContextFactory.serviceContext
def home = context.attributes.thisInstance["home"]
def entryPoint = context.attributes.thisInstance["entryPoint"]
def applicationDir = context.attributes.thisInstance["applicationDir"]
def javaHome = context.attributes.thisInstance["javaHome"]
def jythonHome = context.attributes.thisInstance["jythonHome"]
def jrubyHome = context.attributes.thisInstance["jrubyHome"]

def runMode = config.runMode == "mod" ? "runmod" : "run"

commandLine = new StringBuilder("${runMode} ${entryPoint}")

if (config.cluster)  {
    commandLine << " -cluster"
    if (config.clusterPort) {
    	commandLine << " -cluster-port ${config.clusterPort}"
    }
    if (config.clusterHost) {
    	commandLine << " -cluster-host ${config.clusterHost}"
    }
}

commandLine << (" -instances " + (config.instances ? config.instances : Runtime.runtime.availableProcessors()))

if (config.worker) {commandLine << " -worker"}

println "Executing ${home}/bin/vertx ${commandLine} in directory ${applicationDir}"
new AntBuilder().exec(executable:"${home}/bin/vertx", dir:"${applicationDir}", osfamily:"unix") {
    arg(line:commandLine)
    env(key:"JAVA_HOME", value:javaHome)
    env(key:"JRUBY_HOME", value:jrubyHome)
    env(key:"JYTHON_HOME", value:jythonHome)
}


       

