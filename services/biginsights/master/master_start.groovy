import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import groovy.text.SimpleTemplateEngine

def config = new ConfigSlurper().parse(new File("master-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def env = System.getenv()
def fulladdress= serviceContext.getPrivateAddress()
def hostAddress = fulladdress.split("/")[0]
if(!hostAddress.contains(".")) 
  {              
            hostAddress = "" + java.net.InetAddress.getLocalHost().getHostAddress()   
  }    
println "master_start.groovy: BigInsights is about to start " + hostAddress
def dataNodeService = serviceContext.waitForService(config.dataNodeService, 120, TimeUnit.SECONDS) 
def dataNodeInstances = dataNodeService.waitForInstances(dataNodeService.getNumberOfActualInstances(), 120, TimeUnit.SECONDS )
def secondNameNodeService = serviceContext.waitForService(config.secondNameNodeService, 120, TimeUnit.SECONDS) 
def secondNameNodeInstances = null
if(secondNameNodeService==null)
	secondNameNodeInstances = []
else
	secondNameNodeInstances = secondNameNodeService.waitForInstances(secondNameNodeService.getNumberOfActualInstances(), 120, TimeUnit.SECONDS )
def nodesIPs = []
def dataNodesIPs = []
def secNameNodesIPs = []
def newHostsFile = new File(serviceContext.serviceDirectory + "/hosts")
newHostsFile.write("127.0.0.1	localhost\n")
if(!serviceContext.isLocalCloud()){
	newHostsFile.append(hostAddress + " IP-" + hostAddress.replaceAll("\\.",'-') + "\n")
	println "Adding to hosts file: " + hostAddress + " IP-" + hostAddress.replaceAll("\\.",'-')
	for(instance in dataNodeInstances)
	{
		def hostIP = instance.getHostAddress()
		if(hostIP == "127.0.0.1")
		{
			println("master_start.groovy: skipping data node IP since it is :" + hostIP) 
			continue
		}
		nodesIPs.add(hostIP)
		dataNodesIPs.add(hostIP)
		println "Adding data node " + hostIP;
		newHostsFile.append(hostIP + " IP-" + hostIP.replaceAll("\\.",'-')+"\n")
		println "Adding to hosts file: " + hostIP + " IP-" + hostIP.replaceAll("\\.",'-')
	}
	for(instance in secondNameNodeInstances)
	{
		def hostIP = instance.getHostAddress()
		nodesIPs.add(hostIP)
		secNameNodesIPs.add(hostIP)
		println "Adding data node " + hostIP;
		newHostsFile.append(hostIP + " IP-" + hostIP.replaceAll("\\.",'-')+"\n")
		println "Adding to hosts file: " + hostIP + " IP-" + hostIP.replaceAll("\\.",'-')
	}
	/* use to distribute the hosts file if the host to ip mapping is not available by default.
	for(ipaddr in nodesIPs)
	{
		new AntBuilder().sequential {	
			exec(executable:"scp" , osfamily:"unix", failonerror:"false") {
				arg("line": serviceContext.serviceDirectory + "/hosts root@"+ipaddr + ":/etc/hosts")	
			}
		}
	}*/
}
def folder = new File('/tmp/');
def bigFolder = ""
folder.eachDirMatch(~/biginsights-.*/) { bigFolder =  "/" + folder.name + "/" + it.name}
println ("master_start.groovy: biginsights folder = " + bigFolder)
def binding = ["masterIP":hostAddress,"hbaseIP": hostAddress,"flumeIP": hostAddress,"bidir": bigFolder]
binding.putAll("dataIPs":dataNodesIPs)
if(secNameNodesIPs.size()==0)
	binding.putAll("secondaryNameNodeIP":hostAddress)
else
	binding.putAll("secondaryNameNodeIP":secNameNodesIPs[0])
binding.putAll(config.flatten())
def engine = new SimpleTemplateEngine()
def f = new File('silentInstall.xml.template')
def template = engine.createTemplate(f).make(binding)
def installXmlTxt = template.toString()
new File(bigFolder + '/silentInstallGenerated.xml').write(installXmlTxt);

def BI_RuntimeDir = config.BI_DIRECTORY_PREFIX + "opt/ibm/biginsights"
new AntBuilder().sequential {	
/* write hosts file if the host to ip mapping is not available by default.
	exec(executable:"sudo" , osfamily:"unix", failonerror:"false") {
		arg("line":" cp -f " + serviceContext.serviceDirectory + "/hosts " + "/etc/hosts")
	}
*/
	touch(file:serviceContext.serviceDirectory + "/installationRunning")
	exec(executable:"sudo" , osfamily:"unix", failonerror:"false") {
		arg("line":"-i " + bigFolder + "/silent-install/silent-install.sh " + bigFolder + "/silentInstallGenerated.xml")	
	}
}

if(config.BI_Version!=2)
{
	def cmd = "-i  -e 's/export HADOOP_NAMENODE_OPTS=\\\"/export HADOOP_NAMENODE_OPTS=\\\"-Dcom.sun.management.jmxremote.port=${config.nameNodeJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false /' ${BI_RuntimeDir}/hdm/hadoop-conf-staging/hadoop-env.sh"
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
} else
{
	new AntBuilder().sequential {	
		delete(file:serviceContext.serviceDirectory + "/installationRunning", quiet:"false")
	}
}
    for(instance in dataNodeInstances)                                       
    {                                                                     
         instance.invoke("installationDone")                        
    }                                                       
    for(instance in secondNameNodeInstances)             
   {                                                 
          instance.invoke("installationDone")    
    }                                           
println "master_start.groovy: BigInsights started"
