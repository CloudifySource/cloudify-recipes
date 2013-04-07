# HAProxy 

**Status**: Tested  
**Description**:  HAProxy   
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [Lianping](https://github.com/Lianping)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Linux* sudoer permissions**:	Not required  
**Release Date**: March 8th 2013  


Tested on:
--------

* <strong>CloudStack</strong>: 
 * <strong>CentOs 5</strong>  
 * <strong>CentOs 6</strong>  
 
Synopsis
--------

This folder contains a service recipe for HAProxy, a reliable, high performance TCP/HTTP load balancer. ApacheLB does not support applications using WebSocket, and applications with TCP traffic. HAProxy provides good support for these situations. 


## Registering a service instance to the HAProxy load balancer.

In haproxy-service.groovy, there are fours custom commands: <strong>addNode</strong>, <strong>removeNode</strong>, <strong>addTomcatNode</strong>, and <strong>removeTomcatNode</strong>. The first two commands are for services require balancing TCP traffic, such as RabbitMQ. The other two commands are for Tomcat services.
You need to add a <strong>postStart</strong> lifecycle event to each service that you want its instances to be able to add themselves to the load balancer as members(nodes).
You need to add a  <strong>pretStop</strong> lifecycle event to each service that you want its instances to be able to remove themselves from the load balancer.

<pre><code>
postStart {

	... ...
	
	def loadBalancingService = context.waitForService(loadBalancerName, 180, TimeUnit.SECONDS)
	loadBalancingService.invoke("addNode", name as String, ipAddress as String, port as String)
	
	... ...
	
}

pretStop {

	... ...
	
	def loadBalancingService = context.waitForService(loadBalancerName, 180, TimeUnit.SECONDS)
	loadBalancingService.invoke("removeNode", ipAddress as String, port as String)
	
	... ...
	
}
	
</code></pre> 