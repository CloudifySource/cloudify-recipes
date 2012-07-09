# Pet Clinic 

**Status**: Tested  
**Description**: Pet Clinic  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: http://repository.cloudifysource.org/org/cloudifysource/2.1.1/gigaspaces-cloudify-2.1.1-ga-b1396-361.zip  
**Linux* sudoer permissions**:	Not required  
**Windows* Admin permissions**:  Required on Windows 7    
**Release Date**: July 1st 2012  


Tested on:
--------

* <strong>localCloud</strong>: Windows 7 and CentOs 
* <strong>EC2</strong>: Ubuntu and CentOs 
* <strong>OpenStack</strong>: CentOs 
* <strong>Rackspace</strong>: CentOs 

We disable the requiretty flag in /etc/sudoers on the installed VMs, so that Cloudify will be able to invoke remote ssh commands as a sudoer. This feature will be a part of Cloudify in the near future.
Until then, please use the [Cloud Drivers Repository](https://github.com/CloudifySource/cloudify-cloud-drivers).


Synopsis
--------

This folder contains a service recipe for the Pet Clinic application.

The PetClinic application is a port of the Spring PetClinic application to Grails and MongoDB. It uses the Grails GORM bindings to MongoDB. 
The users of the application are employees of the clinic who, in the course of their work, need to view and manage information regarding veterinarians, the clients, and their pets.

This recipe is comprised of five services:
* [mongod](../../services/mongodb/mongod/README.md)  
* [mongos](../../services/mongodb/mongos/README.md)  
* [mongoConfig](../../services/mongodb/mongoConfig/README.md)  
* [tomcat](../../services/tomcat/README.md) 
* [apacheLB](../../services/apacheLB/README.md) 


