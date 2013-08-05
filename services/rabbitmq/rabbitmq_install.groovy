import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.hyperic.sigar.OperatingSystem

/**
 * Installs rabbitmq on the VM. Before kicks off installation, 
 * the scripts set the hostname and hosts file entries for the VM. 
 * This set up is done before installation, in case the installer refers to the hostname.
 * @author lchen
 *
 */

def installLinuxRabbitmq(context,builder,currVendor,installScript) {
	if ( context.isLocalCloud() ) {
		if ( context.attributes.thisApplication["installing"] == null || context.attributes.thisApplication["installing"] == false ) {
			context.attributes.thisApplication["installing"] = true
		}
		else {
			while ( context.attributes.thisApplication["installing"] == true ) {
				println "rabbitmq_install.groovy: Waiting for yum (on localCloud) to end on another service instance in this application... "
				sleep 10000
			}
		}
	}
	
	builder.sequential {
		echo(message:"rabbitmq_install.groovy: Chmodding +x ${context.serviceDirectory} ...")
		chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")

		echo(message:"rabbitmq_install.groovy: Running ${context.serviceDirectory}/${installScript} os is ${currVendor}...")
		exec(executable: "${context.serviceDirectory}/${installScript}",failonerror: "true")
	}
	
	if ( context.isLocalCloud() ) {
		context.attributes.thisApplication["installing"] = false
		println "rabbitmq_install.groovy: Finished using yum on localCloud"
	}
}

println "rabbitmq_install.groovy: About to install rabbitmq ... "

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('rabbitmq-service.properties').toURL())

def myInstanceID=context.getInstanceId()
def ipAddress
def hostname
def hostEntries = ""
boolean isLocalCloud = context.isLocalCloud()

if (isLocalCloud) {
	ipAddress =InetAddress.getLocalHost().getHostAddress()
	hostname = 'hostname -s'.execute().text
	hostname = hostname.trim() //trim out the trailing "\n"
} else {
	ipAddress =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
	hostname =  InetAddress.getLocalHost().getHostName()

	def rabbitmqInstances = context.attributes.rabbitmq.instances;
	for (i in rabbitmqInstances){
		if (i.ipAddress != null && i.hostname !=null){
			hostEntries += i.ipAddress + "\t" + i.hostname + "\n"
		}
	}
	
	hostEntries += ipAddress + "\t" + hostname
}

println "rabbitmq_install.groovy: Setting ipAddress to ${ipAddress}"
context.attributes.thisInstance["ipAddress"] = "${ipAddress}"

println "rabbitmq_install.groovy: Setting hostname to ${hostname}"
context.attributes.thisInstance["hostname"] = "${hostname}"

println "rabbitmq_install.groovy: hostEntries is ${hostEntries}"

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()

// Set hostname and update hosts file if it is not on local cloud

builder = new AntBuilder()

if (!isLocalCloud){
	builder.sequential {
		exec(executable: "hostname", failonerror: "true"){
			arg(value:"${hostname}")
		}
	}
	
	def hostsFile = new File(config.hostsFile)
	hostsFile.append("\n" + hostEntries)
}

switch (currVendor) {
	case ["Red Hat", "CentOS", "Fedora"]:
	installLinuxRabbitmq(context,builder,currVendor,"installRabbitMq.sh")
	break
	//TODO: Suport for other OS can be added here
}

println "rabbitmq_install.groovy: End"