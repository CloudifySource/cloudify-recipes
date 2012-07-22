import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

println "hbase_start.groovy: BigInsights hbase node is about to start"

new AntBuilder().sequential {	
	chmod(file:"${serviceContext.serviceDirectory}/wait_for_port.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/wait_for_port.sh", osfamily:"unix", failonerror:"true") {
	}
}

println "hbase_start.groovy: BigInsights hbase node is about to end"
