# ApacheLB 

**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**Maintainer**:       Cloudify
**Maintainer email**: cloudifysource@gigaspaces.com
**Contributors**:    N/A    
**License**:      Apache 2.0    
**Latest Version**: 0.8.2.1 (codename "Shave It")    
**Release Date**: V  



**Description**: Apache Load Balancer
**Cloudify versions**: 2.1.1 GA
**Build**: https://s3.amazonaws.com/gigaspaces-repository/org/cloudifysource/2.1.1/gigaspaces-cloudify-2.1.1-m2-b1396.zip
**Requires Linux* sudoer permissions**:	Mandatory
**Requires Windows* Admin permissions**:	No


Tested on: 	localCloud/Windows 7 and CenOs, EC2/Ubuntu and CentOs, OpenStack/CentOs

|_. HHH1 |_. HHH2 |_. HHH3 |_. HHH4 |
| r1d | rd | rd | rd |
| r2d | rd | rd | rd |





Synopsis
--------

This folder contains a service recipe for Apache load balancer.

Its default port is 8090, but it can be modified in the apacheLB-service.properties.

You can enable/disable the usage of a sticky session, by modifying the useStickysession property in apacheLB-service.properties.


> *Important*: <strong>In order to use this recipe, the installing user must be a sudoer in the installed VMs.</strong>


## Registering a service instance to the Apache load balancer.

In apacheLB-service.groovy, there are two custom commands: <strong>addNode</strong> and <strong>removeNode</strong>.
You need to add a <strong>postStart</strong> lifecycle event to each service that you want its instances to be able to add themselves to the load balancer as members(nodes).
You need to add a  <strong>postStop</strong> lifecycle event to each service that you want its instances to be able to remove themselves from the load balancer.

<pre><code>
	lifecycle {

		install "myService_install.groovy"
		start "myService_start.groovy"
		....
	    ...
		def instanceID = context.instanceId
		postStart {			
			def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
			apacheService.invoke("addNode", "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port+"/applicationName", instanceID as String)
		}
		
		postStop {			
			def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
			apacheService.invoke("removeNode", "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port+"/applicationName", instanceID as String)			
		}		
	}
</pre></code>


## Load Testing

In order to test your application under load, you can use the "load" custom command.

<pre><code>
    service {
	
	  name "apacheLB"
	  ...
	  ...
	  
	  customCommands ([
		...
		...
		"load" : "apacheLB-load.groovy"
	  ])
	  
	  ...
	  ...
	}
</pre></code>

Usage :

The following will fire 35000 requests on http://LB_IP_ADDRESS:LB_PORT/ with 100 concurrent requests each time:

   <strong>invoke apacheLB load 35000 100</strong>


The following will fire 20000 requests on http://LB_IP_ADDRESS:LB_PORT/petclinic-mongo with 240 concurrent requests each time:
   <strong>invoke apacheLB load 20000 240 petclinic-mongo</strong>




