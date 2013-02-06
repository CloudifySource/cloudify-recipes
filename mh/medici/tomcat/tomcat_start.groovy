import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def config=new ConfigSlurper().parse(new File("tomcat-service.properties").toURL())

println "tomcat_start.groovy: Calculating mongoServiceHost..."
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
println "tomcat_start.groovy: This tomcat instance ID is ${instanceID}"

def home= serviceContext.attributes.thisInstance["home"]
println "tomcat_start.groovy: tomcat(${instanceID}) home ${home}"

def script= serviceContext.attributes.thisInstance["script"]
println "tomcat_start.groovy: tomcat(${instanceID}) script ${script}"

if ( !(config.dbServiceName) ||  "${config.dbServiceName}"=="NO_DB_REQUIRED") {
	println "Using dummy db host(DUMMY_HOST) and port(0)"
	dbServiceHost="DUMMY_HOST"
	dbServicePort="0"
}
else {
	println "tomcat_start.groovy: waiting for ${config.dbServiceName}..."
	def dbService = serviceContext.waitForService(config.dbServiceName, 20, TimeUnit.SECONDS) 
	def dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 
    dbServiceHost = dbInstances[0].hostAddress
	println "tomcat_start.groovy: ${config.dbServiceName} host is ${dbServiceHost}"
	def dbServiceInstances = serviceContext.attributes[config.dbServiceName].instances
	
	dbServicePort = dbServiceInstances[1].port
	println "tomcat_start.groovy: ${config.dbServiceName} port is ${dbServicePort}"
}

if ( !(config.esServiceName) ||  "${config.esServiceName}"=="NO_ES_REQUIRED") {
	println "Using dummy es host(DUMMY_HOST) and port(0)"
	dbServiceHost="DUMMY_HOST"
	dbServicePort="0"
} 
else {
	println "tomcat_start.groovy: waiting for ${config.esServiceName}..."
	def esService = serviceContext.waitForService(config.esServiceName, 20, TimeUnit.SECONDS) 
	def esInstances = esService.waitForInstances(esService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 

	esServiceHost = esInstances[0].hostAddress
	println "tomcat_start.groovy: ${config.esServiceName} host is ${esServiceHost}"

	def esServiceInstances = serviceContext.attributes[config.esServiceName].instances
	
	esServicePort = esServiceInstances[1].node2NodePort
	println "tomcat_start.groovy: ${config.esServiceName} port is ${esServicePort}"
}

println "tomcat_start.groovy executing ${script}"

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

currJmxPort=config.jmxPort+portIncrement
println "tomcat_start.groovy: jmx port is ${currJmxPort}"

new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix") {
        env(key:"CATALINA_HOME", value: "${home}")
        env(key:"CATALINA_BASE", value: "${home}")
        env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
        env(key:"${config.dbHostVarName}", value: "${dbServiceHost}")
        env(key:"${config.dbPortVarName}", value: "${dbServicePort}")
        env(key:"${config.dbBucketVarName}", value: "${config.dbBucketName}")
        env(key:"${config.esHostVarName}", value: "${esServiceHost}")
        env(key:"${config.esPortVarName}", value: "${esServicePort}")
        env(key:"${config.esClusterVarName}", value: "${config.esClusterName}")
		arg(value:"run")
	}
	exec(executable:"${script}.bat", osfamily:"windows") { 
        env(key:"CATALINA_HOME", value: "${home}")
        env(key:"CATALINA_BASE", value: "${home}")
        env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
        env(key:"${config.dbHostVarName}", value: "${dbServiceHost}")
        env(key:"${config.dbPortVarName}", value: "${dbServicePort}")
        env(key:"${config.dbBucketVarName}", value: "${config.dbBucketName}")
        env(key:"${config.esHostVarName}", value: "${esServiceHost}")
        env(key:"${config.esPortVarName}", value: "${esServicePort}")
        env(key:"${config.esClusterVarName}", value: "${config.esClusterName}")
		arg(value:"run")
	}
}
