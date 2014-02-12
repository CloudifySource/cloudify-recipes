import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("tty.js-service.properties").toURL())

new AntBuilder().sequential {
    mkdir(dir:"${config.installDir}")
    get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
    unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
}

new AntBuilder().sequential {
    chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
    chmod(dir:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/bin",perm:"+x",includes:"*.sh")
    chmod(dir:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/tools/benchmark/bin",perm:"+x",includes:"*.sh")
    exec(executable:"./install.sh", osfamily:"unix",
            output:"install.${System.currentTimeMillis()}.out",
            error:"install.${System.currentTimeMillis()}.err"
    )
}