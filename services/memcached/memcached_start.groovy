import org.cloudifysource.dsl.context.ServiceContextFactory

/**
 * This script starts memcached.
 * 
 * @author lchen
 *
 */

println "memcached_start.groovy: About to start memcached ... "

def config=new ConfigSlurper().parse(new File('memcached-service.properties').toURL())
def context = ServiceContextFactory.getServiceContext()

def instanceID = context.getInstanceId()

boolean isLocalCloud = context.isLocalCloud()

portIncrement = 0
if (isLocalCloud) {
  portIncrement = instanceID - 1
}

def currPort = config.port + portIncrement
context.attributes.thisInstance["port"] = currPort

def ipAddress
if (isLocalCloud) {
	ipAddress =InetAddress.getLocalHost().getHostAddress()
} else {
	ipAddress =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
}

context.attributes.thisInstance["ipAddress"] = ipAddress

context.attributes.thisInstance["nodeName"] = "n${instanceID}"

new AntBuilder().sequential {
	exec(executable:"memcached", osfamily:"unix") {
		arg(value:"-p")
		arg(value:"${currPort}")
		arg(value:"-u")
		arg(value:"${config.user}")
		arg(value:"-d")
	}
}

println "memcached_start.groovy: Finished start of Memcached."