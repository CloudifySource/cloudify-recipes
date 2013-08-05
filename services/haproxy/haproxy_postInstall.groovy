import org.cloudifysource.utilitydomain.context.ServiceContextFactory

/**
 *
 * This scripts configures haproxy.
 * @author lchen
 *
 */

def configureStatisticPort (context, config, configureText) {
	def statisticPort = config.statisticPort
	println "Set the statistic port to ${statisticPort}"
	context.attributes.thisInstance["port"] = statisticPort
	return configureText.replace("8100","${statisticPort}")
}

def configureHttpFrontEnd (context, config, configureText) {
	def frontEndPort = config.frontEndPort
	context.attributes.thisInstance["frontEndPort"] = frontEndPort
	println "replace bind 0.0.0.0:8080 with bind 0.0.0.0:${frontEndPort}"
	return configureText.replace("bind 0.0.0.0:8080","bind 0.0.0.0:${frontEndPort}")
}

def configureTcpFrontEnd (context, config, configureText) {
	
	def ipAddress
	boolean isLocalCloud = context.isLocalCloud()
	
	if (isLocalCloud) {
		ipAddress =InetAddress.getLocalHost().getHostAddress()
	} else {
		ipAddress =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
	}
	
	def frontEndPort = config.frontEndPort
	
	def frontEndIpAndPort = "${ipAddress}:${frontEndPort}"
	println "Set the front-end ip and port to ${frontEndIpAndPort}"
	return configureText.replace("<front-end IP and Port>", frontEndIpAndPort)
}

println "haproxy_postInstall.groovy: About to configure haproxy ... "

context = ServiceContextFactory.getServiceContext()
config=new ConfigSlurper().parse(new File('haproxy-service.properties').toURL())
context.attributes.thisService["port"] = config.frontEndPort

def configFileTemplate = new File ("${context.serviceDirectory}/${config.configFileTemplate}")
def configureText = configFileTemplate.text

configureText = configureStatisticPort(context, config, configureText)
configureText = configureHttpFrontEnd(context, config, configureText)
println "haproxy_postInstall.groovy: after configure http front end, the configure text is: ${configureText}" 
configureText = configureTcpFrontEnd(context, config, configureText)
println "haproxy_postInstall.groovy: after configure tcp front end, the configure text is: ${configureText}"

def configureFile = new File ("${config.configureFile}")
configureFile.createNewFile()
configureFile.text = configureText

println "haproxy_postInstall.groovy: Finished the configuration of haproxy."