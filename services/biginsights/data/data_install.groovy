import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.text.SimpleTemplateEngine

def config = new ConfigSlurper().parse(new File("data-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def env = System.getenv()
def hostAddress = env["CLOUDIFY_AGENT_ENV_PRIVATE_IP"];
new AntBuilder().sequential {
	chmod(file:"${serviceContext.serviceDirectory}/set_ssh_key.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/set_ssh_key.sh", osfamily:"unix") {
		arg("value":config.userPassword)	
		arg("value":config.BI_DIRECTORY_PREFIX)	
	}
}
println "data_install.groovy: Ready for data node installation..."



