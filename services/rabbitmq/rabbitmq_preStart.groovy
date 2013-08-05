import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.UUID

/**
 * Ensure all the nodes in the cluster share the same erlang cookie. 
 * We generate an UUID to use as erlang cookie for each clustser.
 * @author lchen
 *
 */

println "rabbitmq_preSart.groovy: Setup erlang cookie ... "

def config=new ConfigSlurper().parse(new File("rabbitmq-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()
def instanceID = context.getInstanceId()
def erlangCookie

if(context.attributes.thisService.erlangCookie == null){
	erlangCookie = UUID.randomUUID().toString()
	context.attributes.thisService["erlangCookie"] = erlangCookie
} else {
	erlangCookie = context.attributes.thisService["erlangCookie"]
}

println "Setting the erlang cookie of the rabbitmq instance ${instanceID} to ${erlangCookie}"

cookieFile = new File(config.erlangCookieFile)
cookieFile.text = erlangCookie

println "rabbitmq_preSart.groovy: End of setup erlang cookie."