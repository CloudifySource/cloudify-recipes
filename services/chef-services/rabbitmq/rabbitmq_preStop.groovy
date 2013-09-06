import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

/**
 * 
 * Before stop, the node is removed from the load balancer, so that the load balancer will not distribute
 * any requests to this node.
 *   
 * @author lchen
 *
 */

def removeFromLoadBalancer (config, context) {
	def loadBalancerName = config.loadBalancerName
	println "rabbitmq_preStop.groovy: Use load balancer ${loadBalancerName}."
	
	def ipAddress = context.attributes.thisInstance["ipAddress"]
	def port = context.attributes.thisInstance["port"]
	def nodeAddress = ipAddress + ":" + port
	
	println "rabbitmq_preStop.groovy: About to remove ${nodeAddress} from ${loadBalancerName} by invoking the removeNode command with parameters ${ipAddress} ${port} ... "
	
	def loadBalancingService = context.waitForService(loadBalancerName, 180, TimeUnit.SECONDS)
	loadBalancingService.invoke("removeNode", ipAddress as String, port as String)
	
	println "rabbitmq_preStop.groovy: Removed ${nodeAddress} from ${loadBalancerName} by invoking the removeNode command with parameters ${ipAddress} ${port}."
}

println "rabbitmq_preStop.groovy: Start of preStop processing ... "

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('rabbitmq-service.properties').toURL())

boolean useLoadBalancer = config.useLoadBalancer
if (useLoadBalancer){
	removeFromLoadBalancer (config, context)
}

println "rabbitmq_preStop.groovy: End of preStop processing."