import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
/**
 * It was observed that the attribute value set by using the attributes API was not cleared after uninstall of the application.
 * For example, if numberOfInstancesStarted is set to 10. When the application is uninstalled. We install the application again
 * The value of the attribute numberOfInstancesStarted remains to be 10. To fix this issue, the attribute is set to its initial
 * value in the preServiceStart stage. 
 * 
 * @author lchen
 */
println "memcached_preServiceSart: start"

context = ServiceContextFactory.getServiceContext()

println "memcached_preServiceSart: Initialize numberOfInstancesStarted to 0."
context.attributes.thisService["numberOfInstancesStarted"] = 0

println "memcached_preServiceSart: numberOfInstancesStarted: " + context.attributes.thisService["numberOfInstancesStarted"]

println "memcached_preServiceSart: end"