import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext() 
def options = [:]

try {
    config = new ConfigSlurper().parse(new File('${context.serviceName}.properties').toURL())
    if ("chefServerURL" in config) {
        options["serverURL"] = config.chefServerURL
    }
} catch(java.io.FileNotFoundException e) { 
    println "Default config file not found. Skipping"
}
ChefBootstrap.getBootstrap(options).install()
