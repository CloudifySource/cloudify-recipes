import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import util

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("tty.js-service.properties").toURL())

new AntBuilder().sequential {
    replace(file:"${context.serviceDirectory}/my-config.json",token:"<XAP_BIN>",value:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/bin")
}

new AntBuilder().sequential {
    chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
    chmod(dir:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/bin",perm:"+x",includes:"*.sh")
    exec(executable:"./start.sh", osfamily:"unix",
            output:"start.${System.currentTimeMillis()}.out",
            error:"start.${System.currentTimeMillis()}.err"
    ){
        env(key:"XAP_BIN", value:"${context.serviceDirectory}/${config.installDir}/${config.xapDir}/bin")
    }
}