import org.cloudifysource.dsl.context.ServiceContextFactory
import static Shell.*

def context = ServiceContextFactory.getServiceContext()

bootstrap = ChefBootstrap.getBootstrap(installFlavor:"gem")
bootstrap.runSolo([
    "chef_server": [
        "server_url": "http://localhost:8080",
        "init_style": "runit"
    ],
    "chef_packages": [
        "chef": [
            "version": "10.12.0"
        ]
    ],
    "run_list": ["recipe[build-essential]", "recipe[chef-server::rubygems-install]", "recipe[chef-server::apache-proxy]" ]
])

//setting the global attributes to be available for all chef clients  
def privateIp = System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
def serverUrl = "http://${privateIp}:4000" as String
context.attributes.global["chef_validation.pem"] = sudoReadFile("/etc/chef/validation.pem")
context.attributes.global["chef_server_url"] = serverUrl