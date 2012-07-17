import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import org.cloudifysource.usm.USMUtils

jbossMongoConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())

println "jbossMongo_start.groovy: Calculating mongoServiceHost..."
serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()
println "jboss_start.groovy: This jboss instance ID is ${instanceID}"

def dbServiceHost
def dbServicePort

if ( "${jbossMongoConfig.dbServiceName}"!="NO_DB_REQUIRED" ) {
	println "jboss_start.groovy: waiting for ${jbossMongoConfig.dbServiceName}..."
	def dbService = serviceContext.waitForService(jbossMongoConfig.dbServiceName, 20, TimeUnit.SECONDS) 
	def dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 
	dbServiceHost = dbInstances[0].hostAddress
	println "jboss_start.groovy: ${jbossMongoConfig.dbServiceName} host is ${dbServiceHost}"		
	def dbServiceInstances = serviceContext.attributes[jbossMongoConfig.dbServiceName].instances
	dbServicePort = dbServiceInstances[1].port
	println "jboss_start.groovy: ${jbossMongoConfig.dbServiceName} port is ${dbServicePort}"		
}
else {
	dbServiceHost="DUMMY_HOST"
	dbServicePort="DUMMY_PORT"
}	

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

script = "${jbossMongoConfig.home}/bin/standalone"

if(USMUtils.isWindows()) {
	println "jboss_start.groovy: Adding mongo port and host to ${script} .bat ..."
	searchStr="set JAVA_OPTS="
	standAloneFile = new File("${script}.bat") 
	standAloneText = standAloneFile.text	
	replaceStr = "set MONGO_HOST=${dbServiceHost}"+"\n"
	replaceStr = replaceStr + "set MONGO_PORT=${dbServicePort}" +"\n"
	replaceStr = replaceStr + "set JAVA_OPTS=-DMONGO_PORT=%MONGO_PORT% -DMONGO_HOST=%MONGO_HOST% %JAVA_OPTS%"+"\n"
	replaceStr = replaceStr + searchStr
	standAloneFile.text = standAloneText.replace(searchStr, replaceStr) 
}
else {
	println "jboss_start.groovy: Adding mongo port and host to ${script} .sh ..."
	searchStr="DIRNAME="
	standAloneFile = new File("${script}.sh") 
	standAloneText = standAloneFile.text	
	replaceStr = "MONGO_HOST=${dbServiceHost}"+"\n"
	replaceStr = replaceStr + "MONGO_PORT=${dbServicePort}" +"\n"
	replaceStr = replaceStr + "JAVA_OPTS=\"-DMONGO_PORT=\$MONGO_PORT -DMONGO_HOST=\$MONGO_HOST \$JAVA_OPTS\""+"\n"
	replaceStr = replaceStr + searchStr
	standAloneFile.text = standAloneText.replace(searchStr, replaceStr) 
}



println "jboss_start.groovy executing ${script} ..."
new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix") {        
		env(key:"MONGO_HOST", value: "${dbServiceHost}")
        env(key:"MONGO_PORT", value: "${dbServicePort}")
	}
	exec(executable:"${script}.bat", osfamily:"windows") {       
		env(key:"MONGO_HOST", value: "${dbServiceHost}")
        env(key:"MONGO_PORT", value: "${dbServicePort}")
	}
}

println "jboss_start.groovy End of ${script}"







