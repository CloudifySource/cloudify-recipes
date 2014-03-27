import org.cloudifysource.utilitydomain.context.ServiceContextFactory

import java.util.concurrent.TimeUnit

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
ip=context.getPrivateAddress()

locators=""
mgmt=context.waitForService(config.managementService,5,TimeUnit.MINUTES)
if (!(mgmt!=null && mgmt.instances.size())) {
    println "No management services found"
} else {
    lusnum=0
    mgmt.instances.each{
        def lusname="lus${it.instanceId}"
        locators+="${it.hostAddress}:${config.lusPort},"
    }
    println "LOOKUPLOCATORS = ${locators}"
}

new AntBuilder().sequential {
    exec(executable:"./start.sh", osfamily:"unix",
            output:"start.${System.currentTimeMillis()}.out",
            error:"start.${System.currentTimeMillis()}.err"
    ) {
        env(key:"LOOKUPLOCATORS",value:"${locators}")
        env(key:"NIC_ADDR",value:"${ip}")
    }
}