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
import java.util.concurrent.TimeUnit
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig
import org.openspaces.core.util.MemoryUnit
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.elastic.config.ElasticStatefulProcessingUnitConfig
import groovy.util.ConfigSlurper;


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

puurl=context.attributes.thisInstance["deploy-epu-puurl"]
puname=context.attributes.thisInstance["deploy-epu-name"]
schema=context.attributes.thisInstance["deploy-epu-schema"]
partitions=context.attributes.thisInstance["deploy-epu-partitions"]
backups=context.attributes.thisInstance["deploy-epu-backups"]
maxmemmb=context.attributes.thisInstance["deploy-epu-maxmemmb"]
maxcores=context.attributes.thisInstance["deploy-epu-maxcores"]
memcapacity=context.attributes.thisInstance["deploy-epu-memcapacity"]
numcores=context.attributes.thisInstance["deploy-epu-numcores"]

//DEPLOY

// find xap management
mgmt_service=context.waitForService("xap-management",10,TimeUnit.SECONDS)
assert mgmt_service!=null
locators=""
mgmt_service.instances.each{locators+="${it.hostAddress},"}


// find gsm
admin=new AdminFactory().addLocators("127.0.0.1:${config.lusPort}").createAdmin();
gsm=admin.gridServiceManagers.waitForAtLeastOne(10,TimeUnit.SECONDS)
assert gsm!=null

// grab file
new AntBuilder().sequential {	
	mkdir(dir:"lib")
	get(src:"${puurl}", dest:"lib", skipexisting:true)
}

dpmt=new ElasticStatefulProcessingUnitDeployment("lib/${new File(puurl).name}")
dpmt.name(puname)
dpmt.maxMemoryCapacity(maxmemmb.toInteger(),MemoryUnit.MEGABYTES)
dpmt.maxNumberOfCpuCores(maxcores.toInteger())
dpmt.numberOfBackupsPerPartition(backups.toInteger())
dpmt.numberOfPartitions(partitions.toInteger())
dpmt.scale(new ManualCapacityScaleConfig())

gsm.deploy(dpmt.create())
admin.close()
