import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import groovy.text.SimpleTemplateEngine

def config = new ConfigSlurper().parse(new File("master-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def env = System.getenv()
def hostAddress = env["CLOUDIFY_AGENT_ENV_PRIVATE_IP"];

println "master_start.groovy: BigInsights is about to start"
def dataNodeService = serviceContext.waitForService(config.dataNodeService, 120, TimeUnit.SECONDS) 
def dataNodeInstances = dataNodeService.waitForInstances(dataNodeService.getNumberOfActualInstances(), 120, TimeUnit.SECONDS )
def dataNodesIPs = []

/* uncomment for separate hbase and flume servers
def hbaseNodeService = serviceContext.waitForService(config.hbaseNodeService, 120, TimeUnit.SECONDS) 
def hbaseNodeInstances = dataNodeService.waitForInstances(hbaseNodeService.getNumberOfActualInstances(), 120, TimeUnit.SECONDS )

def flumeNodeService = serviceContext.waitForService(config.flumeNodeService, 120, TimeUnit.SECONDS) 
def flumeNodeInstances = dataNodeService.waitForInstances(flumeNodeService.getNumberOfActualInstances(), 120, TimeUnit.SECONDS )
*/

for(instance in dataNodeInstances)
	dataNodesIPs.add(instance.getHostAddress())

//def binding = ["masterIP":"${hostAddress}","hbaseIP":hbaseNodeInstances[0].getHostAddress(),"flumeIP":flumeNodeInstances[0].getHostAddress()]
def binding = ["masterIP":"${hostAddress}","hbaseIP": hostAddress,"flumeIP": hostAddress]
binding.putAll("dataIPs":dataNodesIPs)
binding.putAll(config.flatten())
def engine = new SimpleTemplateEngine()
def f = new File('silentInstall.xml.template')
def template = engine.createTemplate(f).make(binding)
def installXmlTxt = template.toString()
def folder = new File('/tmp/');
def bigFolder = ""
folder.eachDirMatch(~/biginsights-basic-linux64.*/) { bigFolder =  "/" + folder.name + "/" + it.name}
println ("master_install.groovy: biginsights folder = " + bigFolder)
new File(bigFolder + '/silentInstallGenerated.xml').write(installXmlTxt);

def cmd = "-i  -e 's/export HADOOP_NAMENODE_OPTS=\\\"/export HADOOP_NAMENODE_OPTS=\\\"-Dcom.sun.management.jmxremote.port=${config.nameNodeJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false /' ${config.ibmHome}${config.BigInsightInstall}/hadoop-conf/hadoop-env.sh"
new AntBuilder().sequential {	
	touch(file:serviceContext.serviceDirectory + "/installationRunning")
	exec(executable:bigFolder + "/silent-install/silent-install.sh", osfamily:"unix", failonerror:"false") {
		arg("value":bigFolder + "/silentInstallGenerated.xml")	
	}
	exec(executable:"sed", osfamily:"unix", failonerror:"false") {
		arg(line: cmd) 
	}
}

println "master_install.groovy: master was installed"
println "Running  sudo  " + "-i -u biadmin " +config.ibmHome + config.BigInsightInstall + "/bin/stop.sh hadoop" 
new AntBuilder().sequential {	
	exec(executable:"sudo" , osfamily:"unix", failonerror:"false"){
		arg(line:"-i -u biadmin " +config.ibmHome + config.BigInsightInstall + "/bin/stop.sh hadoop")
		}
	exec(executable:"sudo" , osfamily:"unix", failonerror:"false"){
		arg(line:"-i -u biadmin " +config.ibmHome + config.BigInsightInstall + "/bin/start.sh hadoop")
		}
	delete(file:serviceContext.serviceDirectory + "/installationRunning", quiet:"false")
}
println "master_start.groovy: BigInsights started"
