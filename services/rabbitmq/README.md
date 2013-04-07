# RabbitMQ 

**Status**: Tested  
**Description**:  RabbitMQ   
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [Lianping](https://github.com/Lianping)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Linux* sudoer permissions**:  Not required  
**Release Date**: March 28th 2013  


Tested on:
--------

* <strong>Local Cloud</strong>: 
 * <strong>CentOs 6</strong>  


Synopsis
--------

This folder contains a service recipe for RabbitMQ. It supports auto scale of RabbitMQ cluster.

The recipe provides several configuration parameters to contol the behaviour. These configuration parameters can be specified in the configuration file named rabbitmq-service.properties.

|Parameter	|Description
|:------------- |:-------------| -----:|
|port		|The port number of RabbitMQ. On a local cloud this is the port number for the first instance.
|mgmtPort	|The port number for RabbitMQ management console.
|erlangCookieFile |The location of the erlang cookie file.
|numberOfDiskNodes|The number of disk nodes in the cluster. In production, 2 is recommended.
|useLoadBalancer|Indicates whether or not to use load balancer. The value can be true or false. When using a load balancer, the RabbitMQ service will depend on the load balancer service.
|loadBalancerName|The name of the load balancer service. At the moment, it only supports HAProxy as the load-balancer.

## Known Issues

 * It is only tested on CentOS. It does not support other operating systems like Windows, Ubuntu, and etc.  
