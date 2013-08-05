import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.hyperic.sigar.OperatingSystem

/**
 * 
 * This scripts installs haproxy.
 * @author lchen
 *
 */

println "haproxy_install.groovy: start"

context = ServiceContextFactory.getServiceContext() 

context.attributes.thisInstance["reloadingConfiguration"] = false

config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
def installScript = "installHaproxy.sh"

new AntBuilder().sequential {
	echo(message:"haproxy_install.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")

	echo(message:"haproxy_install.groovy: Running ${context.serviceDirectory}/${installScript} os is ${currVendor}...")
	exec(executable: "${context.serviceDirectory}/${installScript}",failonerror: "true")
}

println "haproxy_install.groovy: end"