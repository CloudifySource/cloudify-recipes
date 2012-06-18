# ApacheLB 
This folder contains a service recipe for Apache load balancer.

Its default port is 8090, but it can be modified in the apacheLB-service.properties.

You can enable/disable the usage of a sticky session, by modifying the useStickysession property in apacheLB-service.properties.


> *Important*: In order to use this recipe you must be a sudoer in the installed VMs.


## Registering a service instance to the Apache load balancer.

In apacheLB-service.groovy there are two custom commands: addNode and removeNode.
You need to add a *postStart* lifecycle event to each service that you want its instances to be added to the load balancer as members(nodes).
You need to add a *postStop* lifecycle event to each service that you want its instances to be able to remove themselves from the load balancer.

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
			apacheService.invoke("removeNode", "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port+"/benefits", instanceID as String)			
		}		
	}
</pre></code>





