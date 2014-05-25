import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import util
import java.util.concurrent.TimeUnit

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
thisService=util.getThisService(context)
lusPort=config.lusPort
ip = context.attributes.thisInstance.service_ip
println "IP: ${ip}"

uuid=context.attributes.thisInstance.uuid
if(uuid==null){
    uuid=UUID.randomUUID().toString()
    context.attributes.thisInstance.uuid=uuid
}

//Get locator(s)
println "Waiting (max) 5 minutes for ${config.managementService}"
mgmt=context.waitForService(config.managementService,5,TimeUnit.MINUTES)
assert (mgmt!=null),"No management services found"

mgmtservices = mgmt.waitForInstances(1, 1, TimeUnit.MINUTES)
assert (mgmtservices != null), "Unable to find 1 instance of management services"


locators=""
mgmt.instances.each{
    def lusname="lus${it.instanceId}"
    println "invoking update-hosts with ${it.hostAddress} ${lusname}"
    thisService.invoke("update-hosts",it.hostAddress,lusname as String)
    locators+="${lusname}:${lusPort},"
}
println "locators = ${locators}"

//Add Java to PATH if not on localcloud and JAVA_HOME is not defined. This is the java that cloudify downloads.
system_path = "${System.getenv("PATH")}"
if (!context.isLocalCloud() && System.getenv("JAVA_HOME") == null) {
    system_path = "${System.getenv('HOME')}/java/bin/:${system_path}"
}

new AntBuilder().sequential {
    exec(executable:"./start.sh", osfamily:"unix",
            output:"start.${System.currentTimeMillis()}.out",
            error:"start.${System.currentTimeMillis()}.err",
            failonerror: "true"
    ) {
        env(key:"LOOKUPLOCATORS",value:"${locators}")
        env(key:"NIC_ADDR",value:"${ip}")
        env(key:"EXT_JAVA_OPTIONS", value:"-DUUID=${uuid} -Dcom.gs.multicast.enabled=false -Dcom.gs.multicast.discoveryPort=${config.lusPort} -Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=${config.lusPort} -Dcom.gigaspaces.start.httpPort=${config.httpPort} -Dcom.gigaspaces.system.registryPort=${config.registryPort} -Dcom.gs.transport_protocol.lrmi.bind-port=${config.bindPort}")
        env(key:"RMI_OPTIONS", value:"-Djava.rmi.server.hostname=${ip}")
        env(key:"BF_WORKING_DIRECTORY", value:"${context.serviceDirectory}")
        env(key:"BF_UI_PORT", value:"${config.bf_uiPort}")
        env(key:"GS_GROOVY_HOME", value:"${config.installDir}/${config.xapDir}/tools/groovy/")
        env(key:"BF_SCRIPT",value:"/bin/bash -i ${context.serviceDirectory}/start_tutorial.sh")
        env(key:"PATH", value:"${system_path}")
        env(key:"UUID", value:"${uuid}")
    }
}