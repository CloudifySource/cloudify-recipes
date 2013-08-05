import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("master-service.properties").toURL())


new AntBuilder().sequential {	
    chmod(file:"${context.serviceDirectory}/master_stop.sh", perm:"ugo+rx")
	exec(executable:context.serviceDirectory + "/master_stop.sh", osfamily:"unix", failonerror:"false", spawn:"false") {
		arg("value":config.BI_DIRECTORY_PREFIX)
	}
}

