import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/tomcat-service.properties").toURL())
def instanceId = context.instanceId

println "tomcat_init.groovy: Initialize tomcat..."

// Check mandatory values are filled
if (!config.containsKey('serviceName')) println "tomcat_init.groovy: 'serviceName' is required."
if (!config.containsKey('name')) println "tomcat_init.groovy: 'name' is required."
if (!config.containsKey('zipName')) println "tomcat_init.groovy: 'zipName' is required."
if (!config.containsKey('downloadPath')) println "tomcat_init.groovy: 'downloadPath' is required."
if (!config.containsKey('port')) println "tomcat_init.groovy: 'port' is required."
if (!config.containsKey('ajpPort')) println "tomcat_init.groovy: 'ajpPort' is required."
if (!config.containsKey('shutdownPort')) println "tomcat_init.groovy: 'shutdownPort' is required."
if (!config.containsKey('jmxPort')) println "tomcat_init.groovy: 'jmxPort' is required."
if (!config.containsKey('useLoadBalancer')) println "tomcat_init.groovy: 'useLoadBalancer' is required."

