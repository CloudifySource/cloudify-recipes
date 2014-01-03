# Common Puppet service files
This folder contains common files and a base service recipe for Puppet based services. The idea is of using `extend` to include this recipe and supporting files.

> *Important*: Currently the chef recipes have only been tested on an Ubuntu environment on Amazon EC2. Please make sure to use the [EC2 ubuntu cloud driver](https://github.com/CloudifySource/cloudify-cloud-drivers/tree/master/ec2-ubuntu) when installing services or applications that are based on this recipe

To use this integration, in a service recipe, one might use:

    service {
        extend "../services/puppet"
    }

To run puppet, create a .properties file in the extending service with contents similar to the following:

    puppetRepo = [ 
      "repoType": "git",
      "repoUrl": "https://github.com/Fewbytes/puppet-module-examples.git",
      "classes": ["mysql::ruby": nil, 
                  'mysql::server': ['config_hash': ['root_password': 'foo']]
    ]

Explanation of the puppetRepo parameters above:

1. "repoType" signifies the type of repository where the puppet code is located. This can be "tar", "git" or "svn".
2. "repoUrl" is the public url from which the code should be fetched.
3. "classes" is a nested hash that defines the puppet class to apply to the instance.
4. Instead of "classes", you can provide the parameter "manifestPath" to specify an predefined puppet manifest to apply.

Note that only one of "classes" or "manifestPath" will be used, with priority given to "manifestPath". If neither are defined, the repository will still be fetched but nothing will be applied. Even if no properties are defined, you will still be able to load and apply puppet configuration afterwards via custom commands.


## The PuppetBootstrap class
The PuppetBootstrap class is used to bootstrap puppet. Use the `getBootsrap` factory method to obtain a class instance.

<strong>Factory method</strong>
`getBootsrap()` - factory method.

<strong>Class methods:</strong>
`install` - Install Puppet
`loadManifest(String repoType, String repoUrl)` - Load a repository of puppet manifests and libraries from git, svn or a tarball.
`applyManifest(String manifestPath, String manifestSource)` - Apply a puppet manifest to the node. The manifest source can be either the loaded repository, or the service directory.
`applyClasses(Map classes)` - Apply a hash-map of classes and their parameters to the node.
`puppetExecute(String puppetCode)` - Apply arbitrary puppet code to the node.


## custom puppet features
<strong>Custom facts</strong>
A basic integration of cloudify attributes into puppet is provided by the custom facter script `cloudify_facts` which imports the global attributes, those of the current application, the current service and the current instance. The cloudify attributes are coerced into simple string key-value pairs where the original key hierarchy is flattened, prefixed by the attribute type and delimited by underscores (e.g. the global attribute `["k1": ["k2": "v"]]` would become `"global_k1_k2" = "v"`).
Note that the facts are cached and not updated during the puppet run.

<strong>Custom function</strong>
The `get_cloudify_attribute` custom function performs fetching of cloudify attributes during recipe application and allows interaction with other instance, services and application.
The arguments to the function are:

1. `name` - name of the attribute (no default)
2. `type` - global/application/service/instance (defaults to global)
3. `application` - application name (defaults to current application)
4. `service` - service name (defaults to service service)
5. `instance_id` - instance id (defaults to current instance)

example (the following will write the myname attribute of the `hello-puppet` application):

    file {'my-name':
      path    => '/tmp/myname',
      ensure  => present,
      mode    => 0666,
      content => get_cloudify_attribute('myname', 'application'), 
    }

<strong>Custom type</strong>
The `cloudify_attribute` custom type allows setting a cloudify attributes of any type. The arguments are similar to those of the `get_cloudify_attribute` function:

1. `name` - name of the attribute (no default)
2. `value` - the new value (no default)
3. `type` - global/application/service/instance (defaults to global)
4. `application` - application name (defaults to current application)
5. `service` - service name (defaults to service service)
6. `instance_id` - instance id (defaults to current instance)

example (the following will set/change the myname attribute of the `hello-puppet` application):

    cloudify_attribute { 'myname':
        value => 'Inigo Montoya',
        type => 'application',
        ensure  => present,
        application => 'hello-puppet'
    }
