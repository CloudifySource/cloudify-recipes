import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.Random
import java.util.Date

/**
 * Starts the rabbitmq instance. 
 *   
 * @author lchen
 *
 */

println "rabbitmq_start.groovy: About to start rabbitmq ... "

def config=new ConfigSlurper().parse(new File("rabbitmq-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()

//Set the initial value of this isDisk to false explicitly, as we found sometimes it is initialized to true.
context.attributes.thisInstance["isDisk"] = false  

def instanceID = context.getInstanceId()
def rabbitNodeName = "rabbit"
boolean isLocalCloud = context.isLocalCloud()

portIncrement = 0
if (isLocalCloud) {
  portIncrement = instanceID - 1
}

def currPort = config.port + portIncrement
context.attributes.thisInstance["port"] = currPort

currMgmtPort=config.mgmtPort + portIncrement
context.attributes.thisInstance["mgmtPort"] = currMgmtPort

if (isLocalCloud) {
	if (config.nodeNamingOnLocalCloud.equalsIgnoreCase("random")){
		Random generator = new Random();
		int suffix = generator.nextInt(10000)
		println "Ussing a random number as suffix sequence number: " + suffix
		rabbitNodeName += suffix
	} else {
		println "Using instance ID as sequence number for node name"
		rabbitNodeName += instanceID
	}
	
	context.attributes.thisInstance["rabbitNodeName"] = rabbitNodeName
}

def fullRabbitNodeName = rabbitNodeName + "@" + context.attributes.thisInstance["hostname"]

println "rabbitmq_start.groovy: Start an instance of rabbitmq with the following parameters: RABBITMQ_NODE_PORT=${currPort}; RABBITMQ_NODENAME=${fullRabbitNodeName};RABBITMQ_SERVER_START_ARGS=-rabbitmq_mochiweb listeners [{mgmt,[{port,${currMgmtPort}}]}]"

new AntBuilder().sequential {
	exec(executable:"/usr/sbin/rabbitmq-server", osfamily:"unix") {
		env(key: "RABBITMQ_NODE_PORT", value: "${currPort}")
		env(key: "RABBITMQ_NODENAME", value: "${fullRabbitNodeName}")
		env(key: "RABBITMQ_SERVER_START_ARGS", value: "-rabbitmq_mochiweb listeners [{mgmt,[{port,${currMgmtPort}}]}]")
		arg(value:"-detached")
	}
}

println "rabbitmq_start.groovy: end"
