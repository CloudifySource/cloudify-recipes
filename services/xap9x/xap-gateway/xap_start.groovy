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
import java.io.InputStream
import java.io.BufferedReader
import java.util.Arrays
import java.util.concurrent.TimeUnit
import groovy.util.ConfigSlurper
import org.cloudifysource.dsl.context.ServiceContextFactory
import util

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
ip=InetAddress.getLocalHost().getHostAddress()
uuid=context.attributes.thisInstance.uuid
if(uuid==null){
	uuid=UUID.randomUUID().toString()
	context.attributes.thisInstance.uuid=uuid
}

thisService=util.getThisService(context)

//Get locator(s)
mgmt=context.waitForService(config.managementService,1,TimeUnit.MINUTES)
assert (mgmt!=null && mgmt.instances.size()),"No management services found"
locators=""
lusnum=0

mgmt.instances.each{
	def lusname="lus${it.instanceId}"
println "invoking update-hosts with ${it.hostAddress} ${lusname}"
	thisService.invoke("update-hosts",it.hostAddress,lusname as String)
	locators+="${lusname}:${config.lusPort},"
}
println "locators = ${locators}"

new AntBuilder().sequential {
	exec(executable:"runxap.bat", osfamily:"windows",
		output:"runxap.${System.currentTimeMillis()}.out",
		error:"runxap.${System.currentTimeMillis()}.err"
	){
		env(key:"XAPDIR", value:"${config.installDir}\\${config.xapDir}")
		env(key:"GSC_JAVA_OPTIONS",value:"${config.gsc_jvm_options} -DUUID=${uuid} -Dcom.gs.multicast.enabled=false -Dcom.gs.zones=${context.applicationName}.${context.serviceName}.GATEWAY -Dcom.gs.transport_protocol.lrmi.bind-port=${config.lrmiBindPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.initialDiscoveryPort} -Dcom.gs.jini_lus.locators=${locators}")
		env(key:"LOOKUPLOCATORS",value:"${locators}")
		env(key:"NIC_ADDR",value:"${ip}")
	} 

	chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
	chmod(dir:"${config.installDir}/${config.xapDir}",perm:"+x",includes:"*.sh")
	exec(executable:"./runxap.sh", osfamily:"unix",
		output:"runxap.${System.currentTimeMillis()}.out",
		error:"runxap.${System.currentTimeMillis()}.err"
	){
		env(key:"XAPDIR", value:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}")
		env(key:"GSC_JAVA_OPTIONS",value:"${config.gsc_jvm_options} -DUUID=${uuid} -Dcom.gs.multicast.enabled=false -Dcom.gs.zones=${context.applicationName}.${context.serviceName}.GATEWAY -Dcom.gs.transport_protocol.lrmi.bind-port=${config.lrmiBindPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.initialDiscoveryPort} -Dcom.gs.jini_lus.locators=${locators}")
		env(key:"LOOKUPLOCATORS",value:"${locators}")
		env(key:"NIC_ADDR",value:"${ip}")
	}
}


