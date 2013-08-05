import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("master-service.properties").toURL())

def node = args[0]
def role = args[1]

println "removeNode: About to remove node ${node} role ${role} ..."

println "About to execute " + context.serviceDirectory + "/removeNode.sh "

new AntBuilder().sequential {	
	exec(executable:context.serviceDirectory + "/removenode.sh", osfamily:"unix", failonerror:"false", spawn:"true") {
		arg("value":node)	
		arg("value":role)	
		env("key":"BIGINSIGHTS_HOME", "value":config.BI_DIRECTORY_PREFIX + config.BigInsightInstall)
	}
}
println "Execution will continue in the background: " + context.serviceDirectory + "/removenode.sh "

