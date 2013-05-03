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

// Load the configuration
def catalinaHome = config.catalinaHome? config.catalinaHome : "${context.serviceDirectory}/${config.name}"
def catalinaBase = config.catalinaBase? config.catalinaBase : catalinaHome
def catalinaOpts = config.catalinaOpts? config.catalinaOpts : ""
def javaOpts = config.javaOpts? config.javaOpts : ""
def contextPath = contextPath? contextPath : 
	(context.applicationName != "default")? context.applicationName : "ROOT"

context.attributes.thisInstance["catalinaHome"] = "${catalinaHome}"
println "tomcat_init.groovy: tomcat(${instanceId}) catalinaHome is ${catalinaHome}"
context.attributes.thisInstance["catalinaBase"] = "${catalinaBase}"
println "tomcat_init.groovy: tomcat(${instanceId}) catalinaBase is ${catalinaBase}"
context.attributes.thisInstance["catalinaOpts"] = "${catalinaOpts}"
println "tomcat_init.groovy: tomcat(${instanceId}) catalinaOpts is ${catalinaOpts}"
context.attributes.thisInstance["javaOpts"] = "${javaOpts}"
println "tomcat_init.groovy: tomcat(${instanceId}) javaOpts is ${javaOpts}"
context.attributes.thisInstance["contextPath"] = "${contextPath}"
println "tomcat_init.groovy: tomcat(${instanceId}) contextPath is ${contextPath}"
context.attributes.thisInstance["envVar"] = config.envVar
println "tomcat_init.groovy: tomcat(${instanceId}) envVar is ${config.envVar}"

// 'warUrl' can have been set by a customCommand (if self-healing, auto-scaling, etc.)
warUrl = context.attributes.thisService["warUrl"]
if ( warUrl == null ) {  
	if ( config.containsKey("applicationWarUrl") ) {  
		context.attributes.thisService["warUrl"] = config.applicationWarUrl
	}
}
