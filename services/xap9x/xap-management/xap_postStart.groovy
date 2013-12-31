import groovy.util.ConfigSlurper
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

new AntBuilder().sequential {
    exec(executable:"${context.serviceDirectory}/init.sh", osfamily:"unix",
            output:"init.${System.currentTimeMillis()}.out",
            error:"init.${System.currentTimeMillis()}.err"
    )
    exec(executable:"${context.serviceDirectory}/install.sh", osfamily:"unix",
            output:"install.${System.currentTimeMillis()}.out",
            error:"install.${System.currentTimeMillis()}.err"
    )
    exec(executable:"${context.serviceDirectory}/start.sh", osfamily:"unix",
            output:"start.${System.currentTimeMillis()}.out",
            error:"start.${System.currentTimeMillis()}.err"
    )
}
