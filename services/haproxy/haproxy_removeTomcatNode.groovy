import org.cloudifysource.utilitydomain.context.ServiceContextFactory

println "haproxy_removeTomcatNode.groovy: start"

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())

def applicationNameAndInstanceNumber = args[0] + args[1]

println "haproxy_removeTomcatNode.groovy: removing node ${applicationNameAndInstanceNumber}"

def serverLinePattern = "server ${applicationNameAndInstanceNumber}.*"
println "haproxy_removeTomcatNode.groovy: serverLinePattern: ${serverLinePattern}"

def configureFile = new File ("${config.configureFile}")
def configureText = configureFile.text
configureText = configureText.replaceAll(/$serverLinePattern/, "")
configureFile.text = configureText

println "haproxy_removeTomcatNode.groovy: Reload configuration file ${config.configureFile} ... "

new AntBuilder().sequential {
	echo(message:"haproxy_removeTomcatNode.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")
	
	exec(executable:"${context.serviceDirectory}/reloadConfiguration.sh", osfamily:"unix") {
		arg(value:"${config.configureFile}")
		arg(value:"${config.pidFile}")
	}
}

println "haproxy_removeTomcatNode.groovy: Reloaded configuration file ${config.configureFile}. "

println "haproxy_removeTomcatNode.groovy: end"
