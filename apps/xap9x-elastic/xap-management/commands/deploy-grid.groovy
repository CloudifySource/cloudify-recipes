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
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.config.ProcessingUnitConfig
import org.openspaces.admin.space.SpaceDeployment
import groovy.util.ConfigSlurper;
import org.openspaces.admin.*
import org.openspaces.admin.gsm.*
import org.openspaces.admin.pu.*
import org.openspaces.admin.pu.elastic.config.*
import org.openspaces.admin.pu.elastic.*
import org.openspaces.core.util.*
import org.openspaces.admin.space.*


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

name=context.attributes.thisInstance["deploy-grid-name"]
//schema=context.attributes.thisInstance["deploy-grid-schema"]
partitions=context.attributes.thisInstance["deploy-grid-partitions"]
//backups=context.attributes.thisInstance["deploy-grid-backups"]
//maxpervm=context.attributes.thisInstance["deploy-grid-maxpervm"]
//maxpermachine=context.attributes.thisInstance["deploy-grid-maxpermachine"]

if(name==null)name ="mySpace"
if(partitions==null||partitions.toInteger()<=0)partitions ="13"
//if(backups==null||backups.toInteger()<0)backups="1"
//if(schema==null||schema=="")schema="partitioned-sync2backup"
//if(maxpervm==null||maxpervm.toInteger()<=0)maxpervm="0"
//if(maxpermachine==null||maxpermachine.toInteger()<=0)maxpermachine="0"

//DEPLOY
println "DEPLOYING GRID"
// find gsm
ip=context.getPrivateAddress()
admin=new AdminFactory().useDaemonThreads(true).addLocators("${ip}:${config.lusPort}").createAdmin();
print "will wait 2 minute for finding gsm..."
gsm=admin.gridServiceManagers.waitForAtLeastOne(3,TimeUnit.MINUTES)
assert gsm!=null, "No management services found"

try{
   ProcessingUnit pu = gsm.deploy(
        new ElasticSpaceDeployment(name)
           .memoryCapacityPerContainer(500, MemoryUnit.MEGABYTES)
           .numberOfPartitions(partitions.toInteger())
           .dedicatedMachineProvisioning(
                        new DiscoveredMachineProvisioningConfigurer()
                           .addGridServiceAgentZone("zone1")
                           .removeGridServiceAgentsWithoutZone()
                           .create())
            .scale(new EagerScaleConfigurer().create())
   );
   assert (pu.waitFor(1,3,TimeUnit.MINUTES)),"deployment failed"
}catch(org.openspaces.admin.pu.ProcessingUnitAlreadyDeployedException ex){
   //ignore this error --> not the first instance.
}finally{
  admin.close()
}
return true
