# Common Puppet service files
This folder contains common files and a base service recipe for Puppet based services. The idea is of using `extend` to include this recipe and supporting files.

In the extending service, create a properties file with contents similar to the following:

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

Additional info
---------------
This recipe includes the custom facter script "cloudify_facts" that imports cloudify attributes into puppet facts. The imported attributes are the global attributes, those of the current application, the current service and the current instance. The cloudify attributes are coerced into simple string key-value pairs where the original key hierarchy is flattened and separated by dots (e.g. ["k1": ["k2": "v"]] becomes "k1.k2" = "v").

Tested under Ubuntu and Amazon linux.