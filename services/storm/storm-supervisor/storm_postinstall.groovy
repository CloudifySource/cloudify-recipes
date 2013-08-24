import java.net.InetAddress;
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import util

context=ServiceContextFactory.serviceContext

//Force other supervisors to add me to hosts file

service = null

while (service == null)
{
   println "Locating supervisor service...";
   service = context.waitForService("storm-supervisor", 120, TimeUnit.SECONDS)
}
def instances = null;
while(instances==null)
{
   println "Getting supervisor instances...";
   instances = service.waitForInstances(service.getNumberOfPlannedInstances(), 120, TimeUnit.SECONDS )
}

//add me to others and others to me
def others=[]
println "getting list of other supervisors"
instances.each{
	if(it.instanceId != context.instanceId){
		others.add(it)
	}
}
println "# of other sups= ${others.size()}"

try{

	def locked=util.lockFile("/etc/hosts",10)
	if(!locked)return false

	//add me to me
	println("add me to me")
	"${context.serviceDirectory}/commands/addhost.sh ${context.privateAddress} ${InetAddress.localHost.hostName}".execute()

	//add me to others
	others.each{
		println("add me to others:"+it.hostAddress)
		it.invoke("addhostentry","${context.privateAddress}" as String, "${InetAddress.localHost.hostName}" as String)
	}

	//add others to me
	others.each{
		println("adding other to local hosts:${it.hostName}")
		def hostname=it.hostName
		if(hostname.endsWith(".novalocal"))hostname=hostname.substring(0,hostname.length()-10); //openstack hack for bogus host name
		"${context.serviceDirectory}/commands/addhost.sh ${it.hostAddress} ${hostname}".execute()
	}

}
catch(Exception e){
	e.printStackTrace()
	return false
}
finally{
	util.unlockFile("/etc/hosts")
}

return true
