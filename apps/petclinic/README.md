# Pet Clinic 

**Status**: Tested  
**Description**: Pet Clinic  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    N/A    
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: https://s3.amazonaws.com/gigaspaces-repository/org/cloudifysource/2.1.1/gigaspaces-cloudify-2.1.1-m2-b1396.zip  
**Linux* sudoer permissions**:	Not required  
**Windows* Admin permissions**:  Required on Windows 7    
**Release Date**: July 1st 2012  


Tested on:
--------

* <strong>localCloud</strong>: Windows 7 and CentOs 
* <strong>EC2</strong>: Ubuntu and CentOs 
* <strong>OpenStack</strong>: CentOs 



Synopsis
--------

This folder contains a service recipe for Pet Clinic.

The PetClinic application is a port of the Spring PetClinic application to Grails and MongoDB. It uses the Grails GORM bindings to MongoDB. 
The users of the application are employees of the clinic who, in the course of their work, need to view and manage information regarding veterinarians, the clients, and their pets.

This recipe is comprised of five serices:
* [mongod](../../services/mongodb/mongod/README.md)  
* [mongos](../../services/mongodb/mongos/README.md)  
* [mongoConfig](../../services/mongodb/mongoConfig/README.md)  
* [tomcat](../../services/tomcat/README.md) 
* [apacheLB](../../services/apacheLB/README.md) 


