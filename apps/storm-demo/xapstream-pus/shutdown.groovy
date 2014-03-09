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
import org.openspaces.admin.Admin
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.gsa.GridServiceAgents
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.config.ProcessingUnitConfig
import java.util.concurrent.TimeUnit
import org.cloudifysource.dsl.utils.ServiceUtils

def context=null
try{
context = org.cloudifysource.dsl.context.ServiceContextFactory.getServiceContext()
}
catch(e){
context = org.cloudifysource.utilitydomain.context.ServiceContextFactory.getServiceContext()
}
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/${context.serviceName}-service.properties").toURL())

//VERIFY COLLABORATORS ACTUALLY EXIST
//Get locator(s)
def mgmt=context.waitForService(config.managementService,1,TimeUnit.MINUTES)
assert (mgmt!=null && mgmt.instances.size()),"No management services found"
def locators=""
def lusnum=0

mgmt.instances.each{
	locators+="${it.hostAddress}:${config.lusPort},"
}
println "locators = ${locators}"

def admin=new AdminFactory().useDaemonThreads(true).addLocators(locators).create()
def mgr=admin.getGridServiceManagers().waitForAtLeastOne()

assert (mgr!=null),"Manager not found"

def nimbi=context.waitForService("storm-nimbus",1,TimeUnit.MINUTES)
assert (nimbi!=null),"Nimbus service not found"

//UNDEPLOY STREAMSPACE
mgr.undeploy(config.streamspaceName)


//UNDEPLOY REST API
mgr.undeploy("RESTData")

//UNDEPLOY WORDCOUNT UI
mgr.undeploy("wordcount-ui")

//UNDEPLOY TOPOLOGY
nimbi.invoke("kill" as String,"wordcount" as String)

admin.close()
