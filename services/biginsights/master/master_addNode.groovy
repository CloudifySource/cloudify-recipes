import org.cloudifysource.dsl.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("master-service.properties").toURL())

def node = args[0]
def role= args[1]
def instanceID= args[2]

println "addNode: About to add node ${node} ${role} instance (${instanceID}) ..."

println "About to execute " + context.serviceDirectory + "/addnode.sh "

new AntBuilder().sequential {	
	exec(executable:context.serviceDirectory + "/addnode.sh", osfamily:"unix", failonerror:"false", spawn:"true") {
		arg("value":node)	
		arg("value":role)	
		env("key":"BIGINSIGHTS_HOME", "value":config.ibmHome + config.BigInsightInstall)
	}
}
println "Execution will continue in the background: " + context.serviceDirectory + "/addnode.sh "

