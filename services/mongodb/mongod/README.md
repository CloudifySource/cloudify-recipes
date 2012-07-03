# mongod 

**Status**: Tested  
**Description**: Mongod 2.0.2  
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

This folder contains a service recipe for Mongod.

Its default port is 10001, but it can be modified in the mongod-service.properties.


If you want the mongod service instances to participate in the sharding process, just set the sharded property to true in mongod-service.properties.


