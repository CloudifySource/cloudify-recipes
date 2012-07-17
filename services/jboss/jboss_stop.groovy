import org.cloudifysource.dsl.context.ServiceContextFactory

jbossConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())
context = ServiceContextFactory.getServiceContext()

def currentIP = context.attributes.thisInstance["currentIP"]
def portIncrement =  context.isLocalCloud() ? context.getInstanceId()-1 : 0		
def currJmxPort = jbossConfig.jmxPort + portIncrement



script = "${jbossConfig.home}/bin/jboss-admin"
new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix"){
		arg value:"---controller=${currentIP}:${currJmxPort}"
		arg value:"--connect"
		arg value:"--command=:shutdown"
	}
	exec(executable:"${script}.bat", osfamily:"windows"){
		arg value:"---controller=${currentIP}:${currJmxPort}"
		arg value:"--connect"
		arg value:"--command=:shutdown"
	}
}
