import groovy.util.ConfigSlurper

websphereConfig = new ConfigSlurper().parse(new File("websphere-service.properties").toURL())

println "websphere_uninstall.groovy executing ${websphereConfig.uninstallScript}..."
new AntBuilder().sequential {
	exec(executable:"${websphereConfig.uninstallScript}", osfamily:"unix") {        
		arg(value:"–silent")		
	}	
	delete(dir:"${websphereConfig.installDir}", quiet:true)		
}
 
println "websphere_uninstall.groovy: Ended successfully"



