# Common Chef service files
This folder contains common files and a base service recipe for Chef based services. The idea is using `extend` to include this recipe and supporting files.

> *Important*: Currently the chef recipes have only been tested on an Ubuntu environment on Amazon EC2. Please make sure to use the [EC2 ubuntu cloud driver](https://github.com/CloudifySource/cloudify-cloud-drivers/tree/master/ec2-ubuntu) when installing services or applications that are based on this recipe


## The ChefBootstrap class
The ChefBootstrap class is used to bootstrap chef. Use the `getBootsrap` factory method to obtain a class instance.
<strong>Factory method</strong>
`getBootsrap(installFlavor:"flavor", serverURL:"url")` - factory method.

<strong>Class methods:</strong>
`install` - Install Chef
`runClient(ArrayList runList)` - Run Chef client with run list
`runClient(HashMap initialAttributes)` - Run chef-client with initial attributes (including the run\_list attribute)
`runSolo(HashMap initialAttributes)` - Run chef-solo with initial attributes (including the run\_list attribute) 

Client configuration takes place right before chef-client is actually executed, so `serverURL` is meaningless while chef bootstraping (`start` method) takes place. This allows you to change the Chef server later on.

## Installation flavors
<i>gem</i> - Install from ruby gems
<i>pkg</i> - Install from Opscode repository packages
<i>fatBinary</i> - Install using Opscode's fat binary packages (default)

## Example
In a service recipe, one might use:
<pre><code>
service {
    lifecycle {
        install {
            ChefBootstrap.getBootstrap(serverURL:"http://somehost:4000", installFlavor:"gem").install()
        }
        start {
            ChefBootstrap.getBootstrap(serverURL:"http://somehost:4000", installFlavor:"gem").runClient(runList)
        }
    }
}
</code></pre>

Or better yet, just extend this service:
<pre><code>
service {
    extend "../services/chef"
}
</code></pre>
