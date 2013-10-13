import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.hyperic.sigar.OperatingSystem
import static Shell.*

/**
 * the scripts set the hostname and hosts file entries for the VM. 
 * @author lchen
 *
 */


println "rabbitmq_preInstall.groovy: Pre install settings ... "

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('rabbitmq-service.properties').toURL())

def myInstanceID=context.getInstanceId()

if (myInstanceID == 1){
	println "rabbitmq_postStart.groovy: My Instance ID is: ${myInstanceID}. firstInstanceReady: ${context.attributes.thisService['firstInstanceReady']}. Set it to false."
	context.attributes.thisService["firstInstanceReady"] = false
}

def ipAddress
def hostname
def hostEntries = ""

ipAddress =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
hostname =  InetAddress.getLocalHost().getHostName()

def rabbitmqInstances = context.attributes.rabbitmq.instances;
for (i in rabbitmqInstances){
	if (i.ipAddress != null && i.hostname !=null){
		hostEntries += i.ipAddress + "\t" + i.hostname + "\n"
	}
}

hostEntries += ipAddress + "\t" + hostname

context.attributes.thisInstance["hostname"] = "${hostname}"
context.attributes.thisInstance["ipAddress"] = "${ipAddress}"
context.attributes.thisInstance["port"] = config.port
context.attributes.thisInstance["mgmtPort"] = config.mgmtPort

println "rabbitmq_install.groovy: hostEntries is ${hostEntries}"

// Set hostname and update hosts file if it is not on local cloud
sudo("echo -e \"${hostEntries}\" >> ${config.hostsFile}")

//def hostsFile = new File(config.hostsFile)
//hostsFile.append("\n" + hostEntries)

println "rabbitmq_preInstall.groovy: End"
