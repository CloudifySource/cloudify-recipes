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
import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.text.SimpleTemplateEngine
import java.util.concurrent.TimeUnit

def config = new ConfigSlurper().parse(new File("master-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def env = System.getenv()
def hostAddress = env["CLOUDIFY_AGENT_ENV_PRIVATE_IP"];


def dataNodeService = null
while (dataNodeService == null)
{
	println "Locating data service...";
	dataNodeService = serviceContext.waitForService(config.dataNodeService, 120, TimeUnit.SECONDS) 
}
def dataNodeInstances = null;
def rowCount=0;
while(dataNodeInstances==null)
{
	println "Locating data service instances. Expecting " + dataNodeService.getNumberOfPlannedInstances();
	dataNodeInstances = dataNodeService.waitForInstances(dataNodeService.getNumberOfPlannedInstances(), 120, TimeUnit.SECONDS )
}
rowCount = dataNodeInstances.size();
println "Found data services count " + dataNodeInstances.size() + " out of " +dataNodeService.getNumberOfPlannedInstances();

File dataNodes = new File(config.dataNodesFilePath)
println "Create file " +config.dataNodesFilePath+ " that should contain row count=" + rowCount; 
for(instance in dataNodeInstances)
	dataNodes.append(instance.getHostAddress() + "\n")


new AntBuilder().sequential {
	chmod(file:"${serviceContext.serviceDirectory}/download_bi.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/download_bi.sh", osfamily:"unix") {
		arg("value":"${config.userPassword}")	
		arg("value":"${config.BI_DIRECTORY_PREFIX}")	
		arg("value":"${config.BI_Edition}")
	}
	chmod(file:"${serviceContext.serviceDirectory}/set_ssh_key.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/set_ssh_key.sh", osfamily:"unix") {
		arg("value":config.dataNodesFilePath)			
		arg("value":config.DOMAIN)			
	}
	chmod(file:"${serviceContext.serviceDirectory}/addnode.sh", perm:"ugo+rx")
	chmod(file:"${serviceContext.serviceDirectory}/removenode.sh", perm:"ugo+rx")
}
println "master_install.groovy: Installing master..."


