import org.cloudifysource.dsl.context.ServiceContextFactory
import shell

def context = ServiceContextFactory.getServiceContext() 

bootstrap = ChefBootstrap.getBootstrap(installFlavor:"gem")
bootstrap.runSolo([
    "chef_server": [
        "server_url": "http://localhost:4000",
        "init_style": "runit"
    ],
    "run_list": ["recipe[build-essential]", "recipe[chef-server::rubygems-install]", "recipe[chef-server::apache-proxy]" ]
])


// eventually we will want to use a global attribute
context.attributes.thisApplication["chef_validation.pem"] = shell.sudoReadFile("/etc/chef/validation.pem")

