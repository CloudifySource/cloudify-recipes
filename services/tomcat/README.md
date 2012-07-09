# tomcat 

**Status**: Tested  
**Description**: Tomcat 7.0.23  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: http://repository.cloudifysource.org/org/cloudifysource/2.1.1/gigaspaces-cloudify-2.1.1-ga-b1396-361.zip  
**Linux* sudoer permissions**:	Not required  
**Windows* Admin permissions**:  Not required    
**Release Date**: July 1st 2012  


Tested on:
--------

* <strong>localCloud</strong>: Windows 7 and CentOs 
* <strong>EC2</strong>: Ubuntu and CentOs 
* <strong>OpenStack</strong>: CentOs 
* <strong>Rackspace</strong>: CentOs 



Synopsis
--------

This folder contains a service recipe for Tomcat 7.0.23.

Its default http port is 8080, but it can be modified in the tomcat-service.properties.
Its other ports can be also set in the tomcat-service.properties.

You can enable/disable the usage of load balancer, by modifying the useLoadBalancer property in tomcat-service.properties.
If you set useLoadBalancer to true, then during its postStart event, each tomcat service instance will register itself to a load balancer.
In order to use this option, you need to enable a "brother" load balancer service or have an external load balancer.

The full path from which your application war file should be downloaded, needs to be set in the applicationWarUrl property in tomcat-service.properties.

For example:
applicationWarUrl = "http://repository.cloudifysource.org/org/cloudifysource/2.0.0/petclinic-mongo-example.war"

If the application that you deploy on this service, requires a database, you need to set the db's properties in the tomcat-service.properties: 
The following three properties need to be set: 
* dbServiceName="NO_DB_REQUIRED"
* dbHostVarName="DB_SERVICE_IP"
* dbPortVarName="DB_SERVICE_PORT"
