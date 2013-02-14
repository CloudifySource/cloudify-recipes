import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.text.SimpleTemplateEngine
import java.util.concurrent.TimeUnit

def config = new ConfigSlurper().parse(new File("master-service.properties").toURL())
def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()

new AntBuilder().sequential {
	chmod(file:"${serviceContext.serviceDirectory}/download_bi.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/download_bi.sh", osfamily:"unix") {
		arg("value":"${config.userPassword}")	
		arg("value":"${config.BI_DIRECTORY_PREFIX}")	
		arg("value":"${config.BI_Edition}")
	}
	chmod(file:"${serviceContext.serviceDirectory}/set_ssh_key.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/set_ssh_key.sh", osfamily:"unix") {
	}
	chmod(file:"${serviceContext.serviceDirectory}/addnode.sh", perm:"ugo+rx")
	chmod(file:"${serviceContext.serviceDirectory}/removenode.sh", perm:"ugo+rx")
}
println "master_install.groovy: Installing master..."


