# Chef server installation

Installs the open source Chef server using Chef-Solo. 

## Custom commands
- `updateCookbooks repo_type url inner_path` - update the chef-server cookbook from some (remote) repository. repo_type is usually git or svn.
- `cleanupCookbooks` - delete the local (on the chef server instance) copy of the cookbooks. This command will not remove the cookbooks from the chef server itself.
- `listCookbooks` - pretty self explanatory
- `cleanupNode node_name` - cleanup a node from the chef server. This command can be triggered automatically from a lifecycle event (e.g. shutdown) using this code:
<pre><code>def chefServerService = context.waitForService("chef-server", 180, TimeUnit.SECONDS) 
chefServerService.invoke("cleanupNode", java.net.InetAddress.getLocalHost().getHostName()) 
</code></pre>
- `knife` - run generic knife commands. All argument will be fed to knife cli.

## Supported platforms

chef-server does not support 32bit OS. Make sure you assign a proper image in your recipe. The default image in this recipe is not defined in default cloud recipes!
Currently there is a bug in Opscode's Omnitruck download API when using Amazon Linux.

> *Important*: Currently the chef recipes have only been tested on an Ubuntu environment on Amazon EC2. Please make sure to use the [EC2 ubuntu cloud driver](https://github.com/CloudifySource/cloudify-cloud-drivers/tree/master/ec2-ubuntu) when installing services or applications that are based on this recipe
