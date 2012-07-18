Getting started guide for using Chef in Cloudify
===============================================

This tutorial will help you get started with integrating chef recipes into your cloudify applications. We will assume that you are familiar with the basics of the cloudify platform (if not, we suggest http://www.cloudifysource.org/guide/qsg/quick_start_guide) and the basics of the opscode chef platform (if not, we suggest http://wiki.opscode.com/display/chef/Fast+Start+Guide).

Chef comes in two flavors, a client-server model in which clients pull all relevant data from the server and then push back their newly formed configuration (useful for management of inter-server dependencies), and a simpler chef-solo model in which the clients pull the recipes as a tarball from the network and configure themselves in a stand-alone fashion. The example below will use a chef-server as one of the application's services, similar to what is done in the travel-chef example you can find in the cloudify-recipes repository.

To use the chef-server configuration, you'll need to include the two following cloudify recipes in your application, available in the cloudify-recipes example repository (https://github.com/CloudifySource/cloudify-recipes):
* chef-server: Sets up a single server configured with the chef-server package.
* chef: This is the service that will be extended by all the services to be bootstrapped using chef.

Create a directory for your application. In this tutorial, we will assume that your application is named "fluffy", so you will have apps/fluffy/fluffy.groovy as your application manifest. Let's also assume that apart from the chef-server, you will have a mysql server bootsrapped using chef, and a webapp server bootstrapped directly through cloudify. In this case, fluffy.groovy should have the following contents:
    application {
        name = "fluffy"
        
        service {
            name = "chef-server"
        }
 
        service {
            name = "mysql"
            dependsOn = ["chef-server"]
        }     
          
        service {
            name = "webapp"
            dependsOn = ["mysql"]
        }
    }

Apart from the manifest, apps/fluffy should contain the following subdirectories:
*chef-server: extends the chef-server service and fetches the chef data. Copy this from the cloudify-recipes repository and change fetch_chef_data.sh to point at the repository where you plan to keep the published version of this application. Note that, unlike the case presented here, you can also keep the chef code in a completely separate repository (e.g. if the same chef codebase is used for non-cloudify deployments).
*mysql: an basic cloudify service that does nothing much except to extend the basic chef service. You can copy the groovy file from cloudify-recipes.
*webapp: A non-chef cloudify recipe - outside the scaope of this tutorial.
*roles: will contain a ruby file defining each of the chef roles to be used by the services in our application. In this case it will have only a single file "mysql.rb" with the following contents:
    name "mysql"
    description "mysql master server"
    run_list "recipe[fluffy::mysql]"
    default_attributes "mysql" => { "bind_address" => "0.0.0.0" }
*cookbooks: The chef cookbooks with the code that you need to flesh out the roles you defined. In our case, we will need a fluffy cookbook with a mysql recipe that depends on the mysql cookbook and defines our additional databases/users. You can copy this from cloudify-recipes and you can copy its dependencies from cloudify recipes or the opscode cookbooks repository.

We are now ready to install the application. Launch the cloudify shell and bootstrap a cloud (e.g. "bootstrap-cloud ec2") and then install the application by running "install_application apps/fluffy".

According to our application manifest the chef-server machine will be installed first and as the last part of its installation it will pull the chef cookbooks and roles as we defined in the chef-server service file "fetch_chef_data.sh" (this logic is incorporated as the postStart command "chef_server_loadCookbooks.sh", and is also available as a custom command to be rerun if/when the chef cookbooks are updated) . Afterwards, the fluffy mysql service will install chef and then run the chef_client with the role equal to the service's name (the role we defined in mysql.rb). The chef_run is also available as a custom command for later use (e.g. for updating configuration options).

