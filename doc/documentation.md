Cloudify-Chef integration documentation
=======================================

Introduction
------------

Cloudify 2.1.1 supports the Opscode chef platform as an additional method of conveniently deploying and configuring your cloudify services.

Chef provides a powerful multi-platform framework for configuring O/S and services. It complements Cloudify recipes by handling the scope of intra machine configurations.
Using Chef, the lifecycle of services can be simplified - Chef takes care of keeping service configurations up to date with current specs as defined by roles and cookbooks.

## ChefBootstrap class
The _ChefBootstrap_ class is the heart of the integration. Its task is to install, configure and invoke chef with proper data provided by Cloudify.
### Bootstrap types
Controlled by the _installFlavor_ value in the _options_ argument to the bootstrap class _getBootstrap_ factory method. The default type is "gem", using system-wide ruby gems (rvm support will be added in the future).

#### Packages
The following packages are installed prior to chef: 
For Debian: "ruby-dev", "ruby", "ruby-json", "rubygems", "libopenssl-ruby"
For RHEL and SuSE: "ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"
For Windows: the installer msi comes with all its dependencies pre-packaged.

#### Gems
Only the "chef" gems is needed, and it's installed with the version defined in the Bootstrap options.

#### OmniBus
fatBinaryInstall uses AntBuilder to install from a fat-binary on both unix-like and windows systems. See github.com/opscode/omnibus for details on how to compile such binaries.

#### Ruby flavors
Currently only system ruby (from the "ruby" package) is supported, but rvm support is in the works.


### Methods and usage
_getBootstrap(options=[:])_ is a factory method that returns a bootstrap object (extending _ChefBootstrap_) suitable for the client's OS (currently supporting Debian, RHEL, SuSE and Win). The options hash is used in the creation of the _chefConfig_ and can contain the following fields to overwrite the default properties in --_chef.properties_:
-  _context_ - a cloudify dsl context object (org.cloudifysource.dsl.context)
-  _installFlavor_ - as described above in Bootstrap types
-  _version_ - chef version string (e.g. "10.12.0")
-  _bootstrapCookbooksUrl_ - the url for the tarball allowing bootstrapping of chef itself.
-  _serverURL_ - the url for the chef server for chef-client bootstrapping
-  _validationCert_ - the certificate by which a bootstrapping client is validated against the chef server.

TODO: is there anything else we need to say about validation?

### Getting Chef data back into Cloudify
The _runClient_ and _runSolo_ methods return a Json containing Chef's node attributes. For example, to get the mysql root password generated randomly by chef:
    nodeAttributes = ChefBootstrap.GetBootstrap().runSolo(["recipe[mysql]"])
    println  "Mysql root password is \"${nodeAttributes["mysql"]["server_root_password"]}\""

TODO: mention that the initial argumnents are cached in the instance attributes, and can be passed only during bootstrap. [Yoni: where is it cached? I can't find the code]

Custom command
--------------
_run_chef_ this custom command available through the service allows re-running of chef on a live machine with (possibly) different settings. It takes the following parameters:
- _serviceRunList_: a comma-delimited (without spaces) list of roles and recipes to include (e.g. "role[mysql-server],recipe[mysql::server_ec2]"
- _chefType_: "client" or "solo"
- _cookbookUrl_: the location from which to download the cookbook tarball, (only relevant if using Solo)

Currently supported platforms
-----------------------------
### Tested
- Ubuntu

### Implemented
- Debian
- RedHat/CentOS


travel-chef
-----------

The cloudify-recipes repository at https://github.com/CloudifySource/cloudify-recipes includes the travel-chef example of an application that uses a chef server to launch some of its services. This following section will explain the architecture used for this purpose.

The chef-server cloudify service is launched on a single server and on _start_ bootstraps itself using _runSolo_ with the following packages:
  "recipe[build-essential]", "recipe[chef-server::rubygems-install]", "recipe[chef-server::apache-proxy]"

Then on _postStart_ the chef-server runs the customCommand _updateCookbooks_ defined in the _chef-server_ application (extending the same-named service). This customCommand fetches the chef data to be served from a remote repository. In this case, we keep the chef cookbooks and roles in the same github repo with the cloudify recipes (to change it, modify fetch_chef_data.sh). It then uploads to itself the cookbooks in the "cookbooks" directory and loads the roles in the "roles" directory, and becomes ready to serve the clients.

In addition to the chef-server, we defined two services to use the chef-client: _mysql_ and _app_.
These services are dependent on the _chef_ service, so that after it finished loading the cookbooks and roles, they use the "run_chef.groovy" script to bootstrap with the role that has the same name as the service.
