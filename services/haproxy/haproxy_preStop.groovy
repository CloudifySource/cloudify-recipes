import org.cloudifysource.utilitydomain.context.ServiceContextFactory

/**
 * This scripts stops the haproxy gracefully. We put the logic of stoping haproxy in preStop lifecycle event because
 * during uninstall stop event is not called and the haproxy process returned by the locator will be killed brutally. 
 * @author lchen
 *
 */

println "haproxy_preStop.groovy: About to stop haproxy ... "

config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())
context = ServiceContextFactory.getServiceContext()
new AntBuilder().sequential {
	echo(message:"haproxy_preStop.groovy: Chmodding +x ${context.serviceDirectory} ...")
	chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")
	
	exec(executable:"${context.serviceDirectory}/stopHaproxy.sh", osfamily:"unix") {
		arg(value:"${config.pidFile}")
	}
}

println "haproxy_preStop.groovy: Finished stop of haproxy."