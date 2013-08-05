import org.cloudifysource.utilitydomain.context.ServiceContextFactory

/**
 * This scripts implement the removeNode custome command. This command enables the back end server to remove itself
 * when before it stops, so that haproxy will not distribute further load to it.
 * 
 * To achieve this, the item for this back end server should be removed from haproxy configuration and the modified
 * configuration should be reloaded in a way that has minimal impact on the clients of the cluster.
 * 
 * @author lchen
 *
 */

println "haproxy_removeNode.groovy: start"

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())

println "haproxy_removeNode.groovy: args[0] = a${args[0]}a, args[1] = a${args[1]}a"

def ipAndPort = args[0] + ":" + args[1]

def serverLinePadding = config.serverLinePadding
def serverLinePattern = serverLinePadding + "server.*" + ipAndPort.replace(".", "\\.") + ".*" + "\r?\n?"
println "haproxy_removeNode.groovy: serverLinePattern: ${serverLinePattern}"

def configureFile = new File ("${config.configureFile}")
def configureText = configureFile.text
configureText = configureText.replaceAll(/$serverLinePattern/, "")
configureFile.text = configureText

println "haproxy_removeNode.groovy: Reload configuration file ${config.configureFile} ... "

new AntBuilder().sequential {
	echo(message:"haproxy_removeNode.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")
	
	exec(executable:"${context.serviceDirectory}/reloadConfiguration.sh", osfamily:"unix") {
		arg(value:"${config.configureFile}")
		arg(value:"${config.pidFile}")
	}
}

println "haproxy_removeNode.groovy: Reloaded configuration file ${config.configureFile}. "

println "haproxy_removeNode.groovy: end"