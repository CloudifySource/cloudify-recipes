import org.cloudifysource.dsl.context.ServiceContextFactory

config = new ConfigSlurper().parse(new File("mgt-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()

group = config.lookupgroup
locators = serviceContext.attributes.thisApplication["locators"]
home = serviceContext.attributes.thisApplication["home"]


builder = new AntBuilder()

builder.sequential {	
	chmod(dir:"${home}/bin", perm:'+x', includes:"*.sh")
}

def command = "env - ./run.sh ${locators} ${group} ${home} ${config.gscCount} ${config.gscMemory} ${config.jvmParams} >/dev/null 2>/dev/null"
proc = command.execute()
proc.consumeProcessOutput(System.out, System.err)              
proc.waitFor()