import org.cloudifysource.dsl.context.ServiceContextFactory
import org.hyperic.sigar.OperatingSystem

/**
 *
 * This scripts installs memcached.
 * @author lchen
 *
 */

println "memcached_install.groovy: About to install memcached ... "

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('memcached-service.properties').toURL())

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
def installScript = "installMemcached.sh"

new AntBuilder().sequential {
	echo(message:"memcached_install.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")

	echo(message:"memcached_install.groovy: Running ${context.serviceDirectory}/${installScript} os is ${currVendor}...")
	exec(executable: "${context.serviceDirectory}/${installScript}",failonerror: "true")
}

println "memcached_install.groovy: end"