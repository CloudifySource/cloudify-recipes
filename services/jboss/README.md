# JBoss 

**Status**: Tested  
**Description**: Jboss 7.1.0.Final
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: http://repository.cloudifysource.org/org/cloudifysource/2.1.1/gigaspaces-cloudify-2.1.1-ga-b1400.zip  
**Linux* sudoer permissions**:	Not required  
**Windows* Admin permissions**:  Not required    
**Release Date**: July 16th 2012  


Tested on:
--------

* <strong>localCloud</strong>: Windows 7 and CentOs 
* <strong>EC2</strong>: Ubuntu and CentOs 
* <strong>OpenStack</strong>: CentOs 
* <strong>Rackspace</strong>: CentOs 



Synopsis
--------

This folder contains a service recipe for Jboss 7.1.0.Final.

Its default HTTP and JMX port are 8080 and 9999 respectively , but it can be modified in the jboss-service.properties.


You can enable/disable the usage of load balancer, by modifying the useLoadBalancer property in jboss-service.properties.
If you set useLoadBalancer to true, then during its postStart event, each jboss service instance will register itself to a load balancer.
In order to use this option, you need to enable a "brother" load balancer service or use an external load balancer.

The full path from which your application war file should be downloaded, needs to be set in the applicationWarUrl property in jboss-service.properties.

For example:
applicationWarUrl = "http://repository.cloudifysource.org/org/cloudifysource/sample-apps/petclinic-mongo.war"

If the application that you deploy on this service, requires a database, you need to set the db's properties in the jboss-service.properties: 
The following three properties need to be set: 
* dbServiceName="NO_DB_REQUIRED"
* dbHostVarName="DB_SERVICE_IP"
* dbPortVarName="DB_SERVICE_PORT"
