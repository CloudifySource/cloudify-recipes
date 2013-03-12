# WAS Liberty 8.5 

**Status**: Tested  
**Description**: WAS Liberty  8.5.0.0 
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [aharonmoll](https://github.com/aharonmoll)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      free  
**Build**: http://repository.cloudifysource.org/org/cloudifysource/2.3.1-RELEASE/gigaspaces-cloudify-2.3.1-ga-b3720.zip	            
**Linux sudoer permissions**:	Not required  
**Release Date**: March 11 2013  


Tested on:
--------

* <strong>BYON</strong>: CentOS 6.2

Synopsis
--------

This folder contains a service recipe for WebSphere Liberty 8.5.0.0.

Its default HTTP port is 9080.

The full path from which your application war file should be downloaded, needs to be set in the applicationWarUrl property in wlp-service.properties.

For example:
applicationWarUrl = "http://s3.amazonaws.com/aharon_wlp85/ingensg-wa-rest-application-1.0.0-SNAPSHOT.war"

The application uses: Java 7 u9 and connects to a mock running as a pu on XAP 9.1

To add another WAS Liberty service instance please increase by 1 the "numInstances" in wlp-service.groovy file.

The url to reach the application services is:
http://[hostname]:9080/em-rest/services
