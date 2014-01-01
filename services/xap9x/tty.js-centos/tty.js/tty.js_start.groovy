import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import util

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("tty.js-service.properties").toURL())

//Get locator(s)
mgmt=context.waitForService(config.managementService,1,TimeUnit.MINUTES)
assert (mgmt!=null && mgmt.instances.size()),"No management services found"
locators=""
lusnum=0

thisService=util.getThisService(context)

mgmt.instances.each{
    def lusname="lus${it.instanceId}"
    println "invoking update-hosts with ${it.hostAddress} ${lusname}"
    thisService.invoke("update-hosts",it.hostAddress,lusname as String)
    locators+="${lusname}:${config.lusPort},"
}
println "locators = ${locators}"


new AntBuilder().sequential {
    mkdir(dir:"${config.installDir}")
    get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
    unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
}

new AntBuilder().sequential {
    chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
    chmod(dir:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/tools/benchmark/bin",perm:"+x",includes:"*.sh")
    exec(executable:"./start.sh", osfamily:"unix",
            output:"start.${System.currentTimeMillis()}.out",
            error:"start.${System.currentTimeMillis()}.err"
    ){
        env(key:"BENCHMARK_BIN", value:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/tools/benchmark/bin")
        env(key:"GRID_NAME",value:"${config.gridName}")
        env(key:"POJOS_NUMBER",value:"${config.pojosNumber}")
        env(key:"LOCATORS",value:"${locators}")

    }
}