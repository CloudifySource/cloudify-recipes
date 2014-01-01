import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import util

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("tty.js-service.properties").toURL())

locators = context.attributes.thisApplication["locators"]

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