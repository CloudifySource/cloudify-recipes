import org.cloudifysource.dsl.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("master-service.properties").toURL())

def argsLine = "dfs " + args.join(' ')

println "About to execute " + context.serviceDirectory + "/hadoop.sh "

new AntBuilder().sequential {	
    chmod(file:"${context.serviceDirectory}/hadoop.sh", perm:"ugo+rx")
	exec(executable:context.serviceDirectory + "/hadoop.sh", osfamily:"unix", failonerror:"false", spawn:"false") {
		arg("value":argsLine)
		env("key":"BIGINSIGHTS_HOME", "value":config.ibmHome + config.BigInsightInstall)
	}
}
//println "Execution will continue in the background: " + context.serviceDirectory + "/hadoop.sh " + argsLine

