/**
 * This script starts haproxy.
 * @author lchen
 *
 */

println "haproxy_Start.groovy: About to start haproxy ...  "
println "##################################### haproxy_Start.groovy ############################################################# "
config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())

new AntBuilder().sequential {
	exec(executable:"/usr/sbin/haproxy", osfamily:"unix") {
		arg(value:"-f")
		arg(value:"${config.configureFile}")
	}
}

println "haproxy_Start.groovy: Finished start of haproxy."
