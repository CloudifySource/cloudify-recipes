import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

/**
 * Before stop the rabbitmq instance, the rabbitmq instance first leaves the cluster. 
 * The formal leaving of the cluster is important, otherwise the node may not be able to start again.
 * In the case of disk node, if it does not leave the cluster formally, no new node will be able to join
 * the cluster.
 * 
 * This script also stops the instance gracefully. We put the logic in this lifecycle event because, when 
 * uninstall the service, stop is not called, and the process of rabbitmq is killed. Another option to put
 * stop in shutdown, but this is too late because the rabbitmq process is killed before shutdown is called.
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

def leaveCluster (context, resetType) {
	println "rabbitmq_preStop.groovy: About to leave the cluster and stop rabbitmq gracefully... "
	if (context.isLocalCloud()){
		def fullNodeName = context.attributes.thisInstance['rabbitNodeName'] + "@" + context.attributes.thisInstance['hostname']
		
		println "rabbitmq_preStop.groovy: About to leave the cluster on local cloud (resetType: ${resetType}, fullNodeName: ${fullNodeName}) ... "
		new AntBuilder().sequential {
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"-n")
				arg(value:"${fullNodeName}")
				arg(value:"stop_app")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"-n")
				arg(value:"${fullNodeName}")
				arg(value:"${resetType}")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") { //The graceful stop of rabbitmq should happen in this step. Otherwise, during uninstallation, cloudify will kill the process, which will cuase the start of the node next time fail.
				arg(value:"-n")
				arg(value:"${fullNodeName}")
				arg(value:"stop")
			}
		}
	} else {
		println "rabbitmq_preStop.groovy: About to leave the cluster on cloud (resetType: ${resetType}) ... "
		new AntBuilder().sequential {
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"stop_app")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"${resetType}")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"stop")
			}
		}
	}
	println "rabbitmq_preStop.groovy: Left the cluster and stoped rabbitmq gracefully."
}

println "rabbitmq_preStop.groovy: Start of preStop processing ... "

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('rabbitmq-service.properties').toURL())

boolean useLoadBalancer = config.useLoadBalancer
if (useLoadBalancer){
	removeFromLoadBalancer (config, context)
}

def resetType
int numberOfActualInstances

def rabbitmqService = context.waitForService("rabbitmq", 60, TimeUnit.SECONDS)
if (rabbitmqService != null){
	numberOfActualInstances = rabbitmqService.getNumberOfActualInstances()
} else {
	numberOfActualInstances = 0
}

println "rabbitmq_preStop.groovy: numberOfActualInstances is ${numberOfActualInstances}"

if (numberOfActualInstances < 1){
	resetType = "force_reset"
} else {
	resetType = "reset"
}

leaveCluster (context, resetType)

println "rabbitmq_preStop.groovy: End of preStop processing."