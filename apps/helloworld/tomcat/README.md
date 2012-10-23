# tomcat 

**Status**: Tested  
**Description**: Tomcat 7.0.23  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**: http://repository.cloudifysource.org/org/cloudifysource/2.2.0-RELEASES/gigaspaces-cloudify-2.2.0-ga-b2500.zip  
**Linux* sudoer permissions**:	Not required  
**Windows* Admin permissions**:  Not required    
**Release Date**: October 23rd 2012  


Tested on:
--------

* <strong>localCloud</strong>: Windows 7 
* <strong>EC2</strong>: 
 * <strong>CentOs 5</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"  
.
* <strong>OpenStack</strong>:  
 * <strong>CentOs 5</strong>: imageId "1234" CentOS 5.6, hardwareId "103" standard.large - 4 vCPU /8 GB RAM /240 GB , az-1.region-a.geo-1 


Synopsis
--------

This folder contains a recipe that installs a "Hello World" war file on tomcat.
All the binaries are located in the tomcat folder, so you don't need an Internet connection in order to install it.


Its default http port is 8080, but it can be modified in the tomcat-service.properties.
Its other ports can be also set in the tomcat-service.properties.
