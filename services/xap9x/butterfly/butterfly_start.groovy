import org.cloudifysource.utilitydomain.context.ServiceContextFactory

import java.util.concurrent.TimeUnit

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
if (context.isLocalCloud()) {
    ip = "127.0.0.1"
} else {
    ip = context.getPrivateAddress()
}
println "IP: ${ip}"

//Get locator(s)
println "Waiting (max) 5 minutes for ${config.managementService}"
mgmt=context.waitForService(config.managementService,5,TimeUnit.MINUTES)
assert (mgmt!=null),"No management services found"

mgmtservices = mgmt.waitForInstances(1, 1, TimeUnit.MINUTES)
assert (mgmtservices != null), "Unable to find 1 instance of management services"

println "Invoking get-lookuplocators@xap-management"
locators = mgmt.invoke("get-lookuplocators")[0] as String
println "Result(locators): ${locators}"


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