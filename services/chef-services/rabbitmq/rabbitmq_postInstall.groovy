import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

/**
 * Notify the new node's hostname to other existing nodes in the cluster, 
 * so that other nodes will be able to resovle the new node's hostname when
 * try to communicate to this new node.
 *
 */
 
println "rabbitmq_postInstall.groovy: Post install processing ... "

def context = ServiceContextFactory.getServiceContext()

def myInstanceID = context.getInstanceId()

def ipAddress = context.attributes.thisInstance["ipAddress"]
def hostname = context.attributes.thisInstance["hostname"]

def hostsFileEntry = context.attributes.thisInstance["ipAddress"] + "\t" + context.attributes.thisInstance["hostname"]

def rabbitmqService = context.waitForService(context.getServiceName(), 180, TimeUnit.SECONDS)

// The current instance only has ID assigned, but not marked as an actual instance yet. Thus, it returns 0 when this is the first instance of this service
int numberOfActualInstances = rabbitmqService.getNumberOfActualInstances()

println "numberOfActualInstances: ${numberOfActualInstances}"

if (numberOfActualInstances > 0){
	def rabbitmqInstances = rabbitmqService.waitForInstances(numberOfActualInstances, 180, TimeUnit.SECONDS)
	for (i in rabbitmqInstances){
		if (myInstanceID != i.getInstanceId()){
			println "Invoking addHostFileEntry to add ${hostsFileEntry} to instance ${i.getInstanceId()}'s hosts file"
			i.invoke("addHostFileEntry", ipAddress as String, hostname as String)
		}
	}
}

println "rabbitmq_postInstall.groovy: End post install processing."