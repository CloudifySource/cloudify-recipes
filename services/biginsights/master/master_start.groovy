import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import groovy.text.SimpleTemplateEngine

def config = new ConfigSlurper().parse(new File("master-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def env = System.getenv()
def hostAddress = env["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]

println "master_start.groovy: BigInsights is about to start " + hostAddress
def dataNodeService = serviceContext.waitForService(config.dataNodeService, 120, TimeUnit.SECONDS) 
def dataNodeInstances = dataNodeService.waitForInstances(dataNodeService.getNumberOfActualInstances(), 120, TimeUnit.SECONDS )
def dataNodesIPs = []

for(instance in dataNodeInstances)
{
	dataNodesIPs.add(instance.getHostAddress())
	println "Adding data node " + instance.getHostAddress();
}
def folder = new File('/tmp/');
def bigFolder = ""
folder.eachDirMatch(~/biginsights-.*/) { bigFolder =  "/" + folder.name + "/" + it.name}
println ("master_install.groovy: biginsights folder = " + bigFolder)
def binding = ["masterIP":"${hostAddress}","hbaseIP": hostAddress,"flumeIP": hostAddress,"bidir": bigFolder]
binding.putAll("dataIPs":dataNodesIPs)
binding.putAll(config.flatten())
def engine = new SimpleTemplateEngine()
def f = new File('silentInstall.xml.template')
def template = engine.createTemplate(f).make(binding)
def installXmlTxt = template.toString()
new File(bigFolder + '/silentInstallGenerated.xml').write(installXmlTxt);

def BI_RuntimeDir = config.BI_DIRECTORY_PREFIX + "opt/ibm/biginsights"
def cmd = "-i  -e 's/export HADOOP_NAMENODE_OPTS=\\\"/export HADOOP_NAMENODE_OPTS=\\\"-Dcom.sun.management.jmxremote.port=${config.nameNodeJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false /' ${BI_RuntimeDir}/hdm/hadoop-conf-staging/hadoop-env.sh"
new AntBuilder().sequential {	
	touch(file:serviceContext.serviceDirectory + "/installationRunning")
	exec(executable:bigFolder + "/silent-install/silent-install.sh", osfamily:"unix", failonerror:"false") {
		arg("value":bigFolder + "/silentInstallGenerated.xml")	
	}
}

println "master_install.groovy: master was installed"
println "Running  sudo  " + "-i -u biadmin " +BI_RuntimeDir+ "/bin/stop.sh hadoop" 
new AntBuilder().sequential {	
	exec(executable:"sed", osfamily:"unix", failonerror:"false") {
		arg(line: cmd) 
		}
	exec(executable:"sudo", osfamily:"unix", failonerror:"false") {
		arg(line: "-i -u biadmin  sh -c 'yes |${BI_RuntimeDir}/bin/syncconf.sh hadoop force'")
		}
	exec(executable:"sudo" , osfamily:"unix", failonerror:"false"){
		arg(line:"-i -u biadmin " +BI_RuntimeDir + "/bin/stop.sh hadoop")
		}
	exec(executable:"sudo" , osfamily:"unix", failonerror:"false"){
		arg(line:"-i -u biadmin " + BI_RuntimeDir + "/bin/start.sh hadoop")
		}
	delete(file:serviceContext.serviceDirectory + "/installationRunning", quiet:"false")
}

println "master_start.groovy: BigInsights started"
