import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext() 
ChefBootstrap.getBootstrap(
    installFlavor:"gem"
).install()
