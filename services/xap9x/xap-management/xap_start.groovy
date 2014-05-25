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
import groovy.util.ConfigSlurper;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.openspaces.admin.AdminFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
ip = context.attributes.thisInstance.service_ip
lookuplocators = "${ip}:${config.lusPort}"
context.attributes.thisInstance["xaplookuplocators"] = lookuplocators

println "Private IP: ${ip}, lookuplocators: ${lookuplocators}"

uuid=context.attributes.thisInstance.uuid
if(uuid==null){
    uuid=UUID.randomUUID().toString()
    context.attributes.thisInstance.uuid=uuid
}


//update container nodes if any (restart scenario)
def containerService=context.waitForService(config.containerServiceName,5,TimeUnit.SECONDS)
if(containerService!=null){
    println "invoking update-hosts@${config.containerServiceName} with: "+ip+"=lus${context.instanceId}"
    containerService.invoke("update-hosts",ip,"lus${context.instanceId}" as String)
}
else{
    println "no service ${config.containerServiceName} found"
}

//update butterfly nodes if any (restart scenario)
def butterflyService=context.waitForService(config.butterflyServiceName,5,TimeUnit.SECONDS)
if(butterflyService!=null){
    println "invoking update-hosts@${config.butterflyServiceName} with: "+ip+"=lus${context.instanceId}"
    butterflyService.invoke("update-hosts",ip,"lus${context.instanceId}" as String)
}
else{
    println "no service ${config.butterflyServiceName} found"
}

//Run butterfly if enabled
if (config.butterflyEnabled) {
    if (!context.isLocalCloud()) {
        FileWriter out = new FileWriter("${System.getenv('HOME')}/.bashrc",true);
        out.write("${System.getProperty("line.separator")}");
        out.write("export JAVA_HOME=${System.getenv('HOME')}/java");
        out.close();
    }
    new AntBuilder().sequential {
        exec(executable:"./butterfly_start.sh", osfamily:"unix",
                output:"butterfly_start.${System.currentTimeMillis()}.out",
                error:"butterfly_start.${System.currentTimeMillis()}.err",
                failonerror: "true"
        ) {
            env(key:"LOOKUPLOCATORS",value:"${lookuplocators}")
            env(key:"NIC_ADDR",value:"${ip}")
            env(key:"BF_UI_PORT", value:"${config.bf_uiPort}")
            env(key:"UUID", value:"${uuid}")
        }
    }
}
//Run the start script
new AntBuilder().sequential {
    exec(executable:"runxap.bat", osfamily:"windows",
            output:"runxap.${System.currentTimeMillis()}.out",
            error:"runxap.${System.currentTimeMillis()}.err",
            failonerror: "true"
    ){
        env(key:"XAPDIR", value:"${config.installDir}\\${config.xapDir}")
        env(key:"LUS_JAVA_OPTIONS",value:"${config.lus_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid} -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"GSA_JAVA_OPTIONS",value:"${config.gsm_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid} -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"GSM_JAVA_OPTIONS",value:"${config.gsm_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid} -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"WEBUI_JAVA_OPTIONS",value:"${config.webui_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid}")
        env(key:"LOOKUPLOCATORS",value:"${lookuplocators}")
        env(key:"NIC_ADDR",value:"${ip}")
    }

    chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
    chmod(dir:"${config.installDir}/${config.xapDir}",perm:"+x",includes:"*.sh")
    chmod(dir:"${config.installDir}/${config.xapDir}/tools/gs-webui",perm:"+x",includes:"*.sh")

    exec(executable:"./runxap.sh", osfamily:"unix",
            output:"runxap.${System.currentTimeMillis()}.out",
            error:"runxap.${System.currentTimeMillis()}.err",
            failonerror: "true"
    ){
        env(key:"XAPDIR", value:"${config.installDir}/${config.xapDir}")
        env(key:"LUS_JAVA_OPTIONS",value:"${config.lus_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid} -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"GSA_JAVA_OPTIONS",value:"${config.gsm_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid} -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"GSM_JAVA_OPTIONS",value:"${config.gsm_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid} -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"WEBUI_JAVA_OPTIONS",value:"${config.webui_jvm_options} -Dcom.gs.multicast.enabled=false -DUUID=${uuid}")
        env(key:"LOOKUPLOCATORS",value:"${lookuplocators}")
        env(key:"NIC_ADDR",value:"${ip}")
        env(key:"UUID", value:"${uuid}")
    }
}

println "XAPSTART EXITING"