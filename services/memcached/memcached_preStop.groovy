import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils
import java.util.concurrent.TimeUnit

/**
 * This scripts stops Memcached. Before stop, it removes itself from all tomcat instnaces's memcached session manager
 * configuration.
 * 
 * @author lchen
 *
 */

println "memcached-preStop.groovy: About to stop Memcached ... "

def hasDependantServices (config) {
	return config.containsKey("dependantServices") && config.dependantServices.length() >0
}

/**
 * This function removes this memcached node from all tomcat instances' memcached session manager configuration, 
 * so that they will not attempt to use this node to store session information.
 * 
 * @param context the service context
 */
def removeThisNodeFromTomcatInstances (context, config) {
	def ipAddress = context.attributes.thisInstance["ipAddress"]
	def port = context.attributes.thisInstance["port"]
	
	def dependantServices = config.dependantServices.split(",")
	for (dependantServiceName in dependantServices){
		def dependantService = context.waitForService(dependantServiceName, 30, TimeUnit.SECONDS)
		if (dependantService != null){
			int numberOfActualInstances = dependantService.getNumberOfActualInstances()
			if (numberOfActualInstances > 0){
				def tomcatInstances = dependantService.waitForInstances(numberOfActualInstances, 180, TimeUnit.SECONDS)
				for (i in tomcatInstances){
					println "Invoking removeMemcachedNode to remove ${ipAddress}:${port} from tomcat instance ${i.getInstanceID()}'s memcachedNodes configuration."
					i.invoke("removeMemcachedNode", ipAddress as String, port as String)
				}
			}
		}
	}
}

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('memcached-service.properties').toURL())

if (hasDependantServices (config)) {
	removeThisNodeFromTomcatInstances (context, config)
}

def port = context.attributes.thisInstance["port"];

def memcachedPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=memcached,Args.*.eq=${port}")

println "memcached-preStop.groovy: memcachedPids are ${memcachedPids}"

if (memcachedPids.size() == 1){
	
	def memcachedPid = memcachedPids.get(0)
	println "memcached-preStop.groovy: The PID of Memcached process is: ${memcachedPid}"
	
	new AntBuilder().sequential {
		exec(executable:"/bin/kill", osfamily:"unix") {
			arg(value:"-9")
			arg(value:"${memcachedPid}")
		}
	}
	
} 

println "memcached-preStop.groovy: Finished the stop of Memcached."

