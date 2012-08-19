import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()
def chefServerURL = context.attributes.global["chef_server_url"]
def validationCert = context.attributes.global["chef_validation.pem"]

if (chefServerURL == null) {
    println "Cannot find a chef server URL in global attribtue \"chef_server_url\", aborting"
    System.exit(1)
}

println "Using Chef server URL: ${chefServerURL}"

ChefBootstrap.getBootstrap(
    serverURL: chefServerURL,
    validationCert: validationCert,
    context: context
).runClient([run_list: "role[${context.serviceName}]".toString()])
