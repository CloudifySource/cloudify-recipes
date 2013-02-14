import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def serviceContext = ServiceContextFactory.getServiceContext()
println "data_start.groovy: BigInsights data node is about to start"
println "${serviceContext.serviceDirectory}/wait_for_port.sh"

new AntBuilder().sequential {	
	chmod(file:"${serviceContext.serviceDirectory}/wait_for_port.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/wait_for_port.sh", osfamily:"unix", failonerror:"true") {
	}
}
println "data_start.groovy: BigInsights data node is down"
