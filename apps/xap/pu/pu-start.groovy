import org.cloudifysource.utilitydomain.context.ServiceContextFactory

config = new ConfigSlurper().parse(new File("pu-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()

group = config.lookupgroup
locators = serviceContext.attributes.thisApplication["locators"]
home = serviceContext.attributes.thisService["home"]


builder = new AntBuilder()

builder.sequential {	
	chmod(dir:"${home}/bin", perm:'+x', includes:"*.sh")
}

def command = "env - sudo ./run.sh ${locators} ${group} ${home} ${config.gscCount} ${config.gscMemory} ${config.jvmParams} >/dev/null 2>/dev/null"
proc = command.execute()
proc.consumeProcessOutput(System.out, System.err)
proc.waitFor()