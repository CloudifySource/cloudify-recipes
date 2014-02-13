import java.util.concurrent.TimeUnit
import groovy.util.ConfigSlurper
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())
mgmtService=context.waitForService(config.managementService,2,TimeUnit.MINUTES)
assert (mgmtService!=null && mgmtService.instances.size()),"No management services found"
println "invoking deploy-grid-basic with myDataGrid"
def params = new Object[1]
params[0] = "myDataGrid"
mgmtService.invoke("deploy-grid-basic", params, 3, TimeUnit.MINUTES)