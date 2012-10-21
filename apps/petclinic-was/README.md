# Pet Clinic For WebSphere 

**Status**: Tested  
**Description**: Pet Clinic For WebSphere  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: [Cloudify 2.2.0-rc b2496](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-rc-b2496.zip)  
**Linux* sudoer permissions**:	Not required  
**Release Date**: Oct 17th 2012  


Tested on:
--------

* <strong>EC2</strong>: CentOs 




Synopsis
--------

This folder contains an application recipe for the Pet Clinic application.

The PetClinic application is a port of the Spring PetClinic application to Grails and MongoDB. It uses the Grails GORM bindings to MongoDB. 
The users of the application are employees of the clinic who, in the course of their work, need to view and manage information regarding veterinarians, the clients, and their pets.

This recipe is comprised of four services:
* [mongod](../../services/mongodb/mongod/README.md)  
* [mongos](../../services/mongodb/mongos/README.md)  
* [mongoConfig](../../services/mongodb/mongoConfig/README.md)  
* [websphere](../../services/websphere/README.md) 

In addition to the PetClinic application, this recipe also installs two WebSphere sample applications (Plants and AlbumCatalog).
 
Here are the links (which can also be found in the WebSphere service panel in Cloudify WEB UI once the recipe is deployed) :
 
WebSphere admin Console (user:admin,password:admin):
https://WEBSPHERE_IP_ADDRESS:8084/ibm/console
 
Websphere Plants Sample application : 
http://WEBSPHERE_IP_ADDRESS:8081/PlantsByWebSphere/
 
Websphere AlbumCatalog Sample application : 
http://WEBSPHERE_IP_ADDRESS:8081/AlbumCatalogWeb/AlbumCatalog.jsp
 
PetClinic application : 
http://WEBSPHERE_IP_ADDRESS:8081/petclinic-mongo/

Known issues
------------

* The PetClinic application is deployed successfuly, but when you access it, you get an exception which is, in our opinion, a result of conflicts of JDK issues (Grails' and IBM's).
 

