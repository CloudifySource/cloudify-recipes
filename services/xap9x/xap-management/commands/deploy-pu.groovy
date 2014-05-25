/*******************************************************************************
* Copyright (c) 2014 GigaSpaces Technologies Ltd. All rights reserved
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

import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.config.ProcessingUnitConfig
import groovy.util.ConfigSlurper;


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

puurl=context.attributes.thisInstance["deploy-pu-puurl"]
puname=context.attributes.thisInstance["deploy-pu-puname"]
schema=context.attributes.thisInstance["deploy-pu-schema"]
partitions=context.attributes.thisInstance["deploy-pu-partitions"]
backups=context.attributes.thisInstance["deploy-pu-backups"]
maxpervm=context.attributes.thisInstance["deploy-pu-maxpervm"]
maxpermachine=context.attributes.thisInstance["deploy-pu-maxpermachine"]

//DEPLOY

// find xap management
mgmt_service=context.waitForService("xap-management",10,TimeUnit.SECONDS)
assert mgmt_service!=null


// find gsm
lookuplocators = context.attributes.thisInstance["xaplookuplocators"]
admin=new AdminFactory().useDaemonThreads(true).addLocators("${lookuplocators}").createAdmin();
gsm=admin.gridServiceManagers.waitForAtLeastOne(1,TimeUnit.MINUTES)
assert gsm!=null

// make sure there are GSCs
gscs=admin.gridServiceContainers
gscs.waitFor(1,1,TimeUnit.MINUTES)
assert (gscs.size!=0),"no containers found"

// grab file
new AntBuilder().sequential {	
	mkdir(dir:"lib")
	get(src:"${puurl}", dest:"lib", skipexisting:true)
}

pu=new ProcessingUnitConfig()
pu.setProcessingUnit("lib/${new File(puurl).name}")
pu.setName(puname)
pu.setClusterSchema(schema)
pu.setNumberOfInstances(partitions.toInteger())
pu.setNumberOfBackups(backups.toInteger())
pu.setMaxInstancesPerVM(maxpervm.toInteger())
pu.setMaxInstancesPerMachine(maxpermachine.toInteger())

ac=new ApplicationConfig()
ac.setName("${puname}")
ac.addProcessingUnit(pu)
gsm.deploy(ac)
