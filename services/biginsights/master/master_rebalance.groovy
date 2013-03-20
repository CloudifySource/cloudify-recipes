import org.cloudifysource.dsl.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("master-service.properties").toURL())

println "About to execute " + context.serviceDirectory + "/rebalance.sh "

new AntBuilder().sequential {	
    chmod(file:"${context.serviceDirectory}/rebalance.sh", perm:"ugo+rx")
	exec(executable:context.serviceDirectory + "/rebalance.sh", osfamily:"unix", failonerror:"false", spawn:"false") {
		env("key":"BIGINSIGHTS_HOME", "value":config.BI_DIRECTORY_PREFIX + config.BigInsightInstall)
	}
}

