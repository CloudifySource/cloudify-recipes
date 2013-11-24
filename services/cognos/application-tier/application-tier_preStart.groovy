/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
import java.util.concurrent.TimeUnit

println "application-tier_preStart.groovy: Calculating content-database-service Host..."
def serviceContext = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${serviceContext.serviceDirectory}/application-tier-service.properties").toURL())
instanceID = serviceContext.getInstanceId()
println "application-tier_preStart.groovy: This application-tier instance ID is ${instanceID}"

println "application-tier_preStart.groovy: waiting for content-manager.."
def contentManagerService = serviceContext.waitForService(config.contentManagerServiceName, 90, TimeUnit.SECONDS) 
def contentManagerServiceInstances = contentManagerService.waitForInstances(contentManagerService.numberOfPlannedInstances, 90, TimeUnit.SECONDS) 
def contentManagerHost = contentManagerServiceInstances[0].hostAddress
println "application-tier_preStart.groovy: content-manager host is ${contentManagerHost}"
def contentManagerInstances = serviceContext.attributes[config.contentManagerServiceName].instances
def contentManagerPort = 9300
println "application-tier_preStart.groovy: content-manager port is ${contentManagerPort}"

println "application-tier_preStart.groovy: Replacing default content-manager url with ${contentManagerHost}:${contentManagerPort} in cogstartup.xml..."
cogstartupXmlFile = new File("${serviceContext.serviceDirectory}/cognos/configuration/cogstartup.xml") 
cogstartupXmlText = cogstartupXmlFile.text        
cogstartupXmlText = cogstartupXmlText.replace(">http://localhost:9300/p2pd/servlet<",">http://${contentManagerHost}:${contentManagerPort}/p2pd/servlet<")



println "application-tier_preStart.groovy: waiting for content-manager.."
def gatewayService = null 

while (gatewayService == null) {
   println "Locating ${config.gatewayServiceName} ...";
   gatewayService = serviceContext.waitForService(config.gatewayServiceName, 120, TimeUnit.SECONDS)
}

def gatewayServiceInstances = null

while (gatewayServiceInstances == null) {
   gatewayServiceInstances = gatewayService.waitForInstances(1, 120, TimeUnit.SECONDS )
}


def gatewayHost = gatewayServiceInstances[0].hostAddress
println "application-tier_preStart.groovy: gateway host is ${gatewayHost}"
def gatewayInstances = serviceContext.attributes[config.gatewayServiceName].instances
def gatewayPort = 80
println "application-tier_preStart.groovy: gateway port is ${gatewayPort}"


println "application-tier_preStart.groovy: Replacing default gateway url with ${gatewayHost}:${gatewayPort} in cogstartup.xml..."
cogstartupXmlText = cogstartupXmlText.replace('http://localhost:80/ibmcognos/cgi-bin/cognos.cgi', "http://${gatewayHost}:${gatewayPort}/ibmcognos/cgi-bin/cognos.cgi")

def currPublicIP=serviceContext.getPublicAddress()
println "application-tier_preStart.groovy: Replacing default localhost with public address ${currPublicIP} in cogstartup.xml..."
cogstartupXmlText = cogstartupXmlText.replace('localhost:9300', "${currPublicIP}:9300")
cogstartupXmlFile.write(cogstartupXmlText)
println "application-tier_preStart.groovy: End of cogstartupXml conf"

def jmxPort=config.jmxPort
println "application-tier_preStart.groovy: Setting jmxPort (${jmxPort}) in p2pd_deploy_defaults.properties ..."
p2pdProrpertiesFile =  new File("${serviceContext.serviceDirectory}/cognos/webapps/p2pd/WEB-INF/p2pd_deploy_defaults.properties") 
p2pdProrpertiesText = p2pdProrpertiesFile.text
p2pdProrpertiesText = p2pdProrpertiesText.replace('#rmiregistryport=9999', "rmiregistryport=${jmxPort}")
p2pdProrpertiesFile.write(p2pdProrpertiesText)
println "application-tier_preStart.groovy End"