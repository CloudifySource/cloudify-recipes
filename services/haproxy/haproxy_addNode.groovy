import org.cloudifysource.utilitydomain.context.ServiceContextFactory

/**
 * This scripts implements the addNode custom command. This command enables a new back end server to add itself to the
 * load balancer, so that haproxy can distribute load to it.
 * 
 * To achieve this, the configuration file should be changed and the new configuration should be reloaded in a way that
 * is prompt and has minimal impact on the clients of the cluster.
 *  
 * @author lchen
 *
 */

println "haproxy_addNode.groovy: start"

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())

def nodeAddress = args[0] + " " + args[1] + ":" + args[2]

def nodeAddressPlaceHolder = config.nodeAddressPlaceHolder
def serverLineTemplate = config.serverLineTemplate
def serverLine = serverLineTemplate.replace(nodeAddressPlaceHolder, nodeAddress)

println "haproxy_addNode.groovy: Adding the following line to ${config.configureFile}: ${serverLine}"

def configureFile = new File ("${config.configureFile}")
configureFile.append(System.getProperty("line.separator") + serverLine)

println "haproxy_addNode.groovy: Reload configuration file ${config.configureFile} ... "

new AntBuilder().sequential {
	echo(message:"haproxy_addNode.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")
	
	exec(executable:"${context.serviceDirectory}/reloadConfiguration.sh", osfamily:"unix") {
		arg(value:"${config.configureFile}")
		arg(value:"${config.pidFile}")
	}
}

println "haproxy_addNode.groovy: Reloaded configuration file ${config.configureFile}."
