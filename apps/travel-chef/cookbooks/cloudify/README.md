cloudify Cookbook
=================
This coookbook provides interop between Chef and Cloudify.

Requirements
------------
For the Ohai plugin, use the `ohai` cookbook, you can either add `recipe[cloudify]` or `recipe[ohai]` to your `run_list`.

Attributes
----------
`node['cloudify']['rest_management_url']` - The URL of Cloudify REST API. This is usually something like `http://management-machine-hostname:8100/`. At the moment there is no way to autodectect this (management IP is available from LOOKUPLOCATORS environment variable but the port is not) so just set it up front when launching Chef.

Usage
-----
Currently the Ohai plugin will read Cloudify metadata from environment variables or from `/opt/cloudify/metadata.json` if it exists; Environment variables are *not* set in the current chef bootstrap in cloudify-recipes, so include this snippet in your service recipe:

	import java.util.concurrent.TimeUnit
	import groovy.json.JsonOutput
    // ....
    lifecycle {
	    start {
		    def management_rest_url = 
		    "http://" + \
		    context.admin.processingUnits.waitFor("rest", 1000, TimeUnit.SECONDS).instances[0].machine.hostAddress + \
		    ":8100/"
			Shell.sudo("mkdir -p /opt/cloudify")
			Shell.sudoWriteFile("/opt/cloudify/metadata.json", JsonOutput.toJson([
		        "management_rest_url": management_rest_url,
		        "application_name": context.applicationName, 
		        "service_name": context.serviceName,
		        "instance_id": context.instanceId
		    ]))
		    // .. chef bootstrap, etc.

The cookbook contains `cloudify_attribute` LWRP which can be used to set/unset attributes: 

    cloudify_attribute "foo" do
      value "bar" # not needed when using the unset action
      scope :instance
      action :set # default is set
    end

You can read attributes using the `cloudify_get_attribute` function which is available in recipes:

    # cloudify_get_attribute(scope, attribute, opts={})
    cloudify_get_attribute(:application, "mysql_address")

The `cloudify_get_attribute` function accepts keyword arguments `:application`, `:service` and `:instance`. Use them if you need to get attributes from different contexts.

The cookbook contains a chef report/exception handler which write the result of the chef run (status, updated resources, node attributes) to Cloudify's instance attribute store.
To activate the handler, include the `cloudify::default` recipe handler in your `run_list`.

License and Authors
-------------------
Authors: Avishai Ish-Shalom <avishai@fewbytes.com>

Distributed under the Apache v2 license.
