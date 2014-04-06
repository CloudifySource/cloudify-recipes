import org.cloudifysource.utilitydomain.context.ServiceContextFactory

import java.util.concurrent.TimeUnit

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
ip=context.getPrivateAddress()
println "IP: "+ip
locators=""
println "Waiting (max) 3 minutes for ${config.managementService}"
mgmt=context.waitForService(config.managementService,2,TimeUnit.MINUTES)
if (mgmt == null) {
    println "No management services found"
} else {
    println "Found management services, waiting for 1 instance"
    mgmtservices = mgmt.waitForInstances(1, 1, TimeUnit.MINUTES)
    if (mgmtservices == null) {
        println "Unable to find 1 instance of management services"
    } else {
        lusnum=0
        mgmt.instances.each{
            def lusname="lus${it.instanceId}"
            locators+="${it.hostAddress}:${config.lusPort},"
        }
        println "LOOKUPLOCATORS = ${locators}"
    }
}


new AntBuilder().sequential {
    exec(executable:"./start.sh", osfamily:"unix",
            output:"start.${System.currentTimeMillis()}.out",
            error:"start.${System.currentTimeMillis()}.err"
    ) {
        env(key:"LOOKUPLOCATORS",value:"${locators}")
        env(key:"NIC_ADDR",value:"${ip}")
        env(key:"BF_SCRIPT",value:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/tools/groovy/bin/groovy ${context.serviceDirectory}/script.groovy")
        env(key:"EXT_JAVA_OPTIONS", value:"-Dcom.gs.multicast.enabled=false -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"RMI_OPTIONS", value:"-Djava.rmi.server.hostname=${ip}")
    }
}