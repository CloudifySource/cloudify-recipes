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

println "content-manager_preStart.groovy: Setting JMX..."
def serviceContext = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${serviceContext.serviceDirectory}/content-manager-service.properties").toURL())

cogstartupXmlFile = new File("${serviceContext.serviceDirectory}/cognos/configuration/cogstartup.xml") 
cogstartupXmlText = cogstartupXmlFile.text        

def origJmxStr = "<crn:value xsi:type=\"xsd:int\">0</crn:value>"
def newJmxStr = origJmxStr.replace("0",Integer.toString(config.jmxPort))
cogstartupXmlText = cogstartupXmlText.replace(origJmxStr, newJmxStr)

def currPublicIP =serviceContext.getPublicAddress()
cogstartupXmlText = cogstartupXmlText.replace(">http://localhost:9300/p2pd/servlet<", ">http://${currPublicIP}:9300/p2pd/servlet<")

cogstartupXmlFile.write(cogstartupXmlText)
println "content-manager_preStart.groovy End"







