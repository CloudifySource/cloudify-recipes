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

println "application-gateway_preStart.groovy: Calculating app tier Host..."
def serviceContext = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${serviceContext.serviceDirectory}/application-gateway-service.properties").toURL())
instanceID = serviceContext.getInstanceId()
println "application-gateway_preStart.groovy: This application-gateway instance ID is ${instanceID}"

println "application-gateway_preStart.groovy: waiting for app tier"
def applicationTierService = serviceContext.waitForService(config.appTierServiceName, 90, TimeUnit.SECONDS) 
def applicationTierServiceInstances = applicationTierService.waitForInstances(applicationTierService.numberOfPlannedInstances, 90, TimeUnit.SECONDS) 
//def applicationTierInstances = serviceContext.attributes[config.appTierServiceName].instances
def applicationTierPort = 9300
println "application-gateway_preStart.groovy: app tier port is ${applicationTierPort}"

cogstartupXmlFile = new File("${serviceContext.serviceDirectory}/cognos/configuration/cogstartup.xml") 
cogstartupXmlText = cogstartupXmlFile.text

def origXmlNode = "<crn:item xsi:type=\"xsd:anyURI\" order=\"0\">http://localhost:9300/p2pd/servlet/dispatch/ext</crn:item>"
def placeHoderXmlNode = "<crn:item xsi:type=\"xsd:anyURI\" order=\"INSTANCE_ID\">http://localhost:9300/p2pd/servlet/dispatch/ext</crn:item>"

def nodesXml=""
def currNodeIndex
def currNodeXml
def applicationTierHost

applicationTierServiceInstances.each{
	applicationTierHost = it.hostAddress
	println "application-gateway_preStart.groovy: Replacing current app tier host:port with ${applicationTierHost}:${applicationTierPort} in cogstartup.xml..."
	currNodeIndex = it.instanceId - 1 
	currNodeXml = placeHoderXmlNode.replace("INSTANCE_ID",Integer.toString(currNodeIndex))		
	currNodeXml = currNodeXml.replace("localhost:9300","${applicationTierHost}:${applicationTierPort}")
	nodesXml+= currNodeXml +"\n"
}
     
cogstartupXmlText = cogstartupXmlText.replace(origXmlNode, nodesXml)

def currPublicIP=serviceContext.getPublicAddress()
cogstartupXmlText = cogstartupXmlText.replace("http://localhost:80/ibmcognos/controllerServer","http://${currPublicIP}:80/ibmcognos/controllerServer") 


cogstartupXmlFile.write(cogstartupXmlText)
println "application-gateway_preStart.groovy End"


confHttpdScript="${serviceContext.serviceDirectory}/configureApacheConf.sh"
builder = new AntBuilder()
builder.sequential {
    chmod(file:"${confHttpdScript}", perm:'+x')
	exec(executable:"${confHttpdScript}", osfamily:"unix",failonerror: "true")  {
		arg(value:"${serviceContext.serviceDirectory}")		
		arg(value:"${currPublicIP}")
	}	
}



