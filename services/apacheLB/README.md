# ApacheLB 
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
			apacheService.invoke("addNode", "http://${InetAddress.localHost.hostAddress}:${port}/${context.applicationName}" as String, instanceID as String)
		}
		
		postStop {			
			def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
			apacheService.invoke("removeNode", "http://${InetAddress.localHost.hostAddress}:${port}/${context.applicationName}" as String, instanceID as String)			
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




