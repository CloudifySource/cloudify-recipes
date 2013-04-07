import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

/**
 * 
 * Join the rabbitmq instance to a cluster. Determines whether a node should be a diskNode or not. 
 * The first two nodes will be set as disk node. The number of disk node can be configured in the properties file.
 * If use load balancer, the scripts add the new node to the load balancer configuration. 
 * @author lchen
 *
 */

def joinCluster (context, diskNodeNames, isLocalCloud) {
	println "rabbitmq_postStart.groovy: Abourt to join rabbitmq cluster... "
	if (isLocalCloud){
		def fullNodeName = context.attributes.thisInstance['rabbitNodeName'] + "@" + context.attributes.thisInstance['hostname']
		println "rabbitmq_postStart.groovy: joining cluster on local cloud, the diskNodeNames of the cluster are ${diskNodeNames}"
		new AntBuilder().sequential {
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"-n")
				arg(value:"${fullNodeName}")
				arg(value:"stop_app")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"-n")
				arg(value:"${fullNodeName}")
				arg(value:"reset")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(line:"-n ${fullNodeName} cluster ${diskNodeNames}")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"-n")
				arg(value:"${fullNodeName}")
				arg(value:"start_app")
			}
		}
		
	} else {
		println "rabbitmq_postStart.groovy: joining cluster on cloud, the diskNodeNames of the cluster are ${diskNodeNames}"
		new AntBuilder().sequential {
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"stop_app")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"reset")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(line:"cluster ${diskNodeNames}")
			}
			exec(executable:"/usr/sbin/rabbitmqctl", osfamily:"unix") {
				arg(value:"start_app")
			}
		}
		
	}
	println "rabbitmq_postStart.groovy: End of join rabbitmq cluster."
}

def addToLoadBalancer (config, context, isLocalCloud, rabbitNodeName, hostname) {
	def loadBalancerName = config.loadBalancerName
	println "rabbitmq_postStart.groovy: Use load balancer ${loadBalancerName}."
	def name
	if (isLocalCloud) {
		name = rabbitNodeName
	} else {
		name = hostname
	}
	
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

if (myInstanceID != 1){
	println "rabbitmq_postStart.groovy: My Instance ID is: ${myInstanceID}. firstInstanceReady: ${context.attributes.thisService['firstInstanceReady']}."
	while (!context.attributes.thisService["firstInstanceReady"]){
		println "rabbitmq_postStart.groovy: Wait for the first instance to become ready ... "
		Thread.sleep(5000)
	}
}

boolean isDisk = false
boolean isLocalCloud = context.isLocalCloud()
def hostname = context.attributes.thisInstance["hostname"]
def rabbitNodeName

if (isLocalCloud){
	rabbitNodeName = context.attributes.thisInstance["rabbitNodeName"]
} else {
	rabbitNodeName = "rabbit"
}

def rabbitmqInstances = context.attributes.rabbitmq.instances;
int diskNodesCount = 0

StringBuilder diskNodeNamesBuilder = new StringBuilder()

for (i in rabbitmqInstances){
	if (i.isDisk && i.instanceId != myInstanceID){
		diskNodesCount ++
		if (isLocalCloud) {
			diskNodeNamesBuilder.append("${i.rabbitNodeName}@${i.hostname} ")
		} else {
			diskNodeNamesBuilder.append("rabbit@${i.hostname} ")
		}
	}
}

def numberOfConfiguredDiskNodes

if (config.numberOfDiskNodes != null){
	numberOfConfiguredDiskNodes = config.numberOfDiskNodes
} else {
	numberOfConfiguredDiskNodes = 2
}

println "Disk_Nodes_Count: " + diskNodesCount
println "numberOfConfiguredDiskNodes: " + numberOfConfiguredDiskNodes

if (diskNodesCount < numberOfConfiguredDiskNodes){
	isDisk = true
}

println "Set isDisk for current node to: " + isDisk
context.attributes.thisInstance["isDisk"] = isDisk

if (isDisk){
	diskNodeNamesBuilder.append("${rabbitNodeName}@${hostname} ")
}

def diskNodeNames = diskNodeNamesBuilder.toString().trim()

joinCluster (context, diskNodeNames, isLocalCloud)

boolean useLoadBalancer = config.useLoadBalancer
if (useLoadBalancer){
	addToLoadBalancer(config, context, isLocalCloud, rabbitNodeName, hostname)
}

if (myInstanceID == 1){
	println "rabbitmq_postStart.groovy: First instance is ready."
	context.attributes.thisService["firstInstanceReady"] = true
}

println "rabbitmq_postStart.groovy: End of postStart processing."
