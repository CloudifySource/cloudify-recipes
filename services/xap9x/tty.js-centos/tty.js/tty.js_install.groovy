import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import util

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("tty.js-service.properties").toURL())

new AntBuilder().sequential {
    mkdir(dir:"${config.installDir}")
    get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
    unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
}