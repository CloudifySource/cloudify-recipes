# Pet Clinic Simple 

**Status**: Tested  
**Description**: Pet Clinic Simple  
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



Synopsis
--------

This folder contains a service recipe for the Pet Clinic application.

The PetClinic application is a port of the Spring PetClinic application to Grails and MongoDB. It uses the Grails GORM bindings to MongoDB. 
The users of the application are employees of the clinic who, in the course of their work, need to view and manage information regarding veterinarians, the clients, and their pets.

This recipe is comprised of two services:
* [mongod](../../services/mongodb/mongod/README.md)  
* [tomcat](../../services/tomcat/README.md) 

