import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

/**
 * This scripts informs the tomcat instances of the newly created memcached node after initial setup, so that the tomcat instances can 
 * use it to store session data. It is worth note that this only applies to the memcached nodes created in auto-scale, because the 
 * initial memcachned nodes are automatically picked up by tomcat instances during during tomcat start up.
 *  
 * @author lchen
 *
 */

println "memcached_postStart: About to add this new memcached node to all tomcat instances ..."

/**
 * 
 * This function calcuates a boolean value that is used to decide whether to invoke the addMemcachedNode command of tomcat instances.
 * 
 * @param context the service context
 * @return A boolean value to indicate whether the initial instances have been started. It is used to distinguish the instances
 * created in the initial startup and those created during auto-scale.
 */

def hasInitialInstancesStarted (context, config) {
	def initialClusterSize = config.initialCluserSize
	println "memcached_postStart: Initial Cluster Size: initialClusterSize ${initialClusterSize}"
	println "memcached_postStart: numberOfInstancesStarted: " + context.attributes.thisService["numberOfInstancesStarted"]
	return context.attributes.thisService["numberOfInstancesStarted"] >= initialClusterSize
}

def hasDependantServices (config) {
	return config.containsKey("dependantServices") && config.dependantServices.length() >0
}

def addThisMemcachedNodeToTomcatConfiguration (context, config) {
	def nodeName = context.attributes.thisInstance["nodeName"]
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
					println "Invoking addMemcachedNode to add ${nodeName}:${ipAddress}:${port} to instance ${i.getInstanceID()}'s memcachedNodes configuration"
					i.invoke("addMemcachedNode", nodeName as String, ipAddress as String, port as String)
				}
			}
		}
	}
}

/**
 * Record the status of the instances in the cluster. They are used to determine wether to invoke the addMemcachedNode command of tomcat.
 * @param context the service context
 */

def recordStatus (context) {
	context.attributes.thisService["numberOfInstancesStarted"] = context.attributes.thisService["numberOfInstancesStarted"] + 1
}

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('memcached-service.properties').toURL())

def initialInstancesStarted = hasInitialInstancesStarted(context, config)
def hasDependantServices = hasDependantServices (config)

println "memcached_postStart: initialInstancesStarted = ${initialInstancesStarted}"
println "memcached_postStart: hasDependantServices = ${hasDependantServices}"

if (initialInstancesStarted && hasDependantServices) {
	addThisMemcachedNodeToTomcatConfiguration (context, config)
}

recordStatus(context)

println "memcached_postStart: End"