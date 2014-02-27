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
import java.util.concurrent.TimeUnit
import java.util.UUID
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.config.ProcessingUnitConfig
import org.openspaces.admin.space.SpaceDeployment
import groovy.util.ConfigSlurper;
import groovy.text.SimpleTemplateEngine
import org.openspaces.core.gateway.GatewayTarget
import org.openspaces.admin.space.Space
import util


def context=ServiceContextFactory.serviceContext
def config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

def targets=args[0]

assert (targets!=null),"no targets supplied"
targets=targets.split(',')

//Get locator(s)
println "getting locators for ${config.managementService}"
mgmt=context.waitForService(config.managementService,1,TimeUnit.MINUTES)
assert mgmt!=null,"No management services found"
locators=""
lusnum=0
println "found ${mgmt.instances.length} mgmt instances"
mgmt.instances.each{
	def lusname="lus${it.instanceId}"
	locators+="${lusname}:${config.lusPort},"
}
println "locators = ${locators}"
assert locators!="","failed to get locators"

//UNDEPLOY PU

// find gsm
def admin=new AdminFactory().useDaemonThreads(true).addLocators(locators).createAdmin();
def gsm=admin.gridServiceManagers.waitForAtLeastOne(1,TimeUnit.MINUTES)
assert gsm!=null

//undeploy
gsm.undeploy(puname)

// remove gateway target(s) from space
targets.each{target->
		try{
		  space.getReplicationManager().removeGatewayTarget(target)
		  println "removed target ${target}"
  		}
		catch(exc){}
	}
}


return true
