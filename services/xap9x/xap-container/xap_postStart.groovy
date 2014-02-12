import java.util.concurrent.TimeUnit
import groovy.util.ConfigSlurper
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
mgmtService=context.waitForService(config.managementService,1,TimeUnit.MINUTES)
println "invoking deploy-grid-basic with myDataGrid"
mgmtService.invoke("deploy-grid-basic", "myDataGrid" as String)
