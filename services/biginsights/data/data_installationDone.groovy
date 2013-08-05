import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()

println "rm " + context.serviceDirectory + "/installationRunning"

new AntBuilder().sequential {	
	delete(file:context.serviceDirectory + "/installationRunning", quiet:"false")
}

