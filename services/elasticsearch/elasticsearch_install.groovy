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
import java.util.concurrent.TimeUnit;
import org.cloudifysource.dsl.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("elasticsearch-service.properties").toURL())

def instanceID = context.instanceId

context.attributes.thisInstance["myHostAddress"]=context.getPrivateAddress()
println "elasticsearch_install.groovy: myHostAddress is " + context.attributes.thisInstance["myHostAddress"]


def userHome = System.getProperty("user.home")
println "elasticsearch_install.groovy: userHome folder is ${userHome}"
def installationFolder = userHome + File.separator + "${config.serviceName}"
context.attributes.thisInstance["installationFolder"]=installationFolder
println "elasticsearch_install.groovy: installationFolder is ${installationFolder}"



def portIncrement =  context.isLocalCloud() ? instanceID-1 : 0			
def httpPort = config.httpPort + portIncrement
def node2NodePort = config.node2NodePort + portIncrement


println "elasticsearch_install.groovy: current instance's httpPort is ${httpPort}"
context.attributes.thisInstance["httpPort"] = httpPort

println "elasticsearch_install.groovy: current instance's node 2 node Port is ${node2NodePort}"
context.attributes.thisInstance["node2NodePort"] = node2NodePort

context.attributes.thisInstance["scriptsFolder"] = "${context.serviceDirectory}" + File.separator + "scripts"
def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
println "elasticsearch_install.groovy: scriptsFolder folder is ${scriptsFolder}"


println "elasticsearch_install.groovy: Waiting for all instances' addresses for the unicast..."
elasticsearchService = context.waitForService("elasticsearch", 720, TimeUnit.SECONDS)
if (elasticsearchService == null) {
	throw new IllegalStateException("elasticsearch service not found");
}
elasticsearchInstances = elasticsearchService.waitForInstances(elasticsearchService.numberOfPlannedInstances, 720, TimeUnit.SECONDS) 

if (elasticsearchInstances == null) {
	throw new IllegalStateException("elasticsearch service instances are not ready. They must be available in the context, prior to starting this service. Increasing the timeout in waitForInstances above, might solve it");
}
	
unicastsHosts =""	
elasticsearchInstances.each {
	unicastsHosts += it.hostAddress+","
	println "elasticsearch_install.groovy: elasticsearch #"+it.instanceID + " adding " + it.hostAddress
}

unicastsHostsLen=unicastsHosts.length()
if ( unicastsHostsLen > 0 && unicastsHosts.endsWith(",")) {	
	unicastsHosts = unicastsHosts.substring(0,unicastsHostsLen-1)
}

println "elasticsearch_install.groovy: elasticsearch #"+instanceID + ": unicastsHosts is ${unicastsHosts}"

builder = new AntBuilder()


def elasticsearchZip = config.elasticsearchZip
println "elasticsearch_install.groovy: elasticsearch Zip is ${elasticsearchZip}"

def installScript = "install.sh"
builder.sequential {
	echo(message: "elasticsearch_install.groovy: Chmodding +x ${scriptsFolder} ...")
	chmod(dir: "${scriptsFolder}", perm:"+x", includes:"*.sh")

	echo(message: "elasticsearch_install.groovy: Running ${scriptsFolder}/${installScript}...")
	exec(executable: "${scriptsFolder}/${installScript}",failonerror: "true") {
		arg(value:elasticsearchZip)		
		arg(value:installationFolder)	
		arg(value:httpPort)	
		arg(value:node2NodePort)	
		arg(value:unicastsHosts)	
	}
}

println "elasticsearch_install.groovy: End of installation"