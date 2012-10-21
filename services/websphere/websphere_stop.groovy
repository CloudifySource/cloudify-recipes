import groovy.util.ConfigSlurper


websphereConfig = new ConfigSlurper().parse(new File("websphere-service.properties").toURL())

println "websphere_stop.groovy: Executing ${websphereConfig.stopScript} ..."

new AntBuilder().sequential {
	exec(executable:"${websphereConfig.stopScript}", osfamily:"unix") {        
		arg(value:"server1")
		arg(value:"-quiet")
		arg(value:"-password")
		arg(value:"admin")
		arg(value:"-username")
		arg(value:"admin")		     
	}
}

println "websphere_stop.groovy: Ended successfully"







