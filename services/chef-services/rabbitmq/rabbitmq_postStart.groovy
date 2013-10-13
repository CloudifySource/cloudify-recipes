import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

/**
 * 
 * If use load balancer, the scripts add the new node to the load balancer configuration. 
 * @author lchen
 *
 */

def addToLoadBalancer (config, context, isLocalCloud, rabbitNodeName, hostname) {
	def loadBalancerName = config.loadBalancerName
	println "rabbitmq_postStart.groovy: Use load balancer ${loadBalancerName}."
	def name = hostname
	
	def ipAddress = context.attributes.thisInstance["ipAddress"]
	def port = context.attributes.thisInstance["port"]
	def nodeAddress = name + " " + ipAddress + ":" + port
	
	println "rabbitmq_postStart.groovy: About to add ${nodeAddress} to ${loadBalancerName} by invoking the addNode command with parameters ${name} ${ipAddress} ${port} ... "
	
	def loadBalancingService = context.waitForService(loadBalancerName, 180, TimeUnit.SECONDS)
	loadBalancingService.invoke("addNode", name as String, ipAddress as String, port as String)
	
	println "rabbitmq_postStart.groovy: Added ${nodeAddress} to ${loadBalancerName} by invoking the addNode command with parameters ${name} ${ipAddress} ${port}."
}

println "rabbitmq_postStart.groovy: Start of postStart processing ... "

config=new ConfigSlurper().parse(new File('rabbitmq-service.properties').toURL())
context = ServiceContextFactory.getServiceContext()
def myInstanceID=context.getInstanceId()

def hostname = context.attributes.thisInstance["hostname"]

boolean useLoadBalancer = config.useLoadBalancer
if (useLoadBalancer){
	addToLoadBalancer(config, context, false, "rabbit", hostname)
}

// This flag is used to coordinate instance start sequence to form the cluster correctly.
if (myInstanceID == 1){
	println "rabbitmq_postStart.groovy: First instance is ready."
	context.attributes.thisService["firstInstanceReady"] = true
}

println "rabbitmq_postStart.groovy: End of postStart processing."
