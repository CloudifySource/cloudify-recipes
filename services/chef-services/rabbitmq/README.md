# RabbitMQ 

**Status**: Tested  
**Description**:  RabbitMQ recipe using Chef.  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [Lianping](https://github.com/Lianping)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Linux* sudoer permissions**:  Not required  
**Release Date**: July 4th 2013  


Tested on:
--------


* <strong>RHEL 6.2</strong>  


Synopsis
--------

This folder contains a service recipe for RabbitMQ using Chef for installation, start, and etc. It supports auto scale out of RabbitMQ cluster.

The recipe provides several configuration parameters to control the behaviour. These configuration parameters can be specified in the configuration file named rabbitmq-service.properties.

|Parameter	|Description
|:------------- |:-------------| -----:|
|port		|The port number of RabbitMQ. On a local cloud this is the port number for the first instance.
|mgmtPort	|The port number for RabbitMQ management console.
|numberOfDiskNodes|The number of disk nodes in the cluster. In production, 2 is recommended.
|useLoadBalancer|Indicates whether or not to use load balancer. The value can be true or false. When using a load balancer, the RabbitMQ service will depend on the load balancer service.
|loadBalancerName|The name of the load balancer service. At the moment, it only supports HAProxy as the load-balancer.
|runParams	|Specify run_list and various attributes for Chef managed nodes.