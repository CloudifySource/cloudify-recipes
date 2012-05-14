import org.cloudifysource.dsl.context.ServiceContextFactory
def context = ServiceContextFactory.getServiceContext()
def chef_server_service = context.waitForService("chef-server", 20, TimeUnit.SECONDS)
def chefServerURL = "http://${chef_server_service.getInstances()[0].getHostName()}:4000".toString()
def validationCert = context.attributes.thisApplication["chef_validation.pem"]

ChefBootstrap.getBootstrap(
    serverURL: chefServerURL,
    validationCert: validationCert    
).runClient([run_list: "role[${context.thisService.getName()}]".toString()])
