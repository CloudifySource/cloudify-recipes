import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.text.SimpleTemplateEngine
import java.util.concurrent.TimeUnit

def config = new ConfigSlurper().parse(new File("master-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def env = System.getenv()
def hostAddress = env["CLOUDIFY_AGENT_ENV_PRIVATE_IP"];

new AntBuilder().sequential {
	chmod(file:"${serviceContext.serviceDirectory}/download_bi.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/download_bi.sh", osfamily:"unix") {
		arg("value":"${config.userPassword}")	
	}
	chmod(file:"${serviceContext.serviceDirectory}/set_ssh_key.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/set_ssh_key.sh", osfamily:"unix") {
//		arg("value":"/tmp/gs-files/hp-cloud-demo.pem")	
		arg("value":"${System.properties["user.home"]}/gs-files/hp-cloud-demo.pem")	
		arg("value":"${System.properties["user.home"]}/.ssh/identity")	
	}
	chmod(file:"${serviceContext.serviceDirectory}/addnode.sh", perm:"ugo+rx")
	chmod(file:"${serviceContext.serviceDirectory}/removenode.sh", perm:"ugo+rx")
}
println "master_install.groovy: Installing master..."


