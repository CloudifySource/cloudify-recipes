import groovy.util.ConfigSlurper
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())


new AntBuilder().sequential {
    chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
    chmod(dir:"${config.installDir}/${config.xapDir}/tty.js",perm:"+x",includes:"*.sh")
    exec(executable:"./tty.js/init.sh", osfamily:"unix",
            output:"init.${System.currentTimeMillis()}.out",
            error:"init.${System.currentTimeMillis()}.err"
    )
    exec(executable:"./tty.js/install.sh", osfamily:"unix",
            output:"install.${System.currentTimeMillis()}.out",
            error:"install.${System.currentTimeMillis()}.err"
    )
    exec(executable:"./tty.js/start.sh", osfamily:"unix",
            output:"start.${System.currentTimeMillis()}.out",
            error:"start.${System.currentTimeMillis()}.err"
    )
}
