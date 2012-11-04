# LAMP

**Status**: Tested  
**Description**: LAMP Sample Application  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:  [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**Demo**: [cloudifysourcetv](http://www.cloudifysource.org/cloudifysourcetv#6Li_lCZXaKY)   
**License**:      Apache 2.0   
**Build**:  [Cloudify 2.2.0 M4](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m4-b2493-77.zip)   
**Linux* sudoer permissions**:	Mandatory     
**Release Date**: August 31st 2012  


Tested on:
--------

* <strong>EC2</strong>: 
 * <strong>CentOs 5</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"  
 * <strong>Ubuntu 11.10</strong>: "us-east-1/ami-e1aa7388", hardwareId "m1.small", locationId "us-east-1"  
 * <strong>Ubuntu 12.04</strong>: imageId "us-east-1/ami-82fa58eb", hardwareId "m1.small", locationId "us-east-1"  
.
* <strong>OpenStack</strong>:  
 * <strong>CentOs 5</strong>: imageId "1234" CentOS 5.6, hardwareId "103" standard.large - 4 vCPU /8 GB RAM /240 GB , az-1.region-a.geo-1 

We disable the requiretty flag in /etc/sudoers on the installed VMs, so that Cloudify will be able to invoke remote ssh commands as a sudoer. This feature will be a part of Cloudify in the near future.
Until then, please use the [Cloud Drivers Repository](https://github.com/CloudifySource/cloudify-cloud-drivers). 


Synopsis
--------

This folder contains a service recipe for the [LAMP(Linux, Apache HTTP Server, MySQL and PHP)](http://en.wikipedia.org/wiki/LAMP_%28software_bundle%29) 

This recipe is comprised of three services:
* [apache](../../services/apache/README.md) 
* [mysql](../../services/mysql/README.md) 
* [apacheLB](../../services/apacheLB/README.md) 

The LAMP recipe installs a small sample application that uses php code which retrieves data from a MySQL DB(used by this recipe).

The LAMP recipe also installs a [Hangman game (HTML 5)](https://01.org/html5webapps/webapps/hangonman).

URLs (accessible from the apacheLB and apache service panel):  
* http://LB_IP_ADDRESS:LB_PORT/balancer-manager - LB console  
* http://LB_IP_ADDRESS:LB_PORT/index.php - PHP info  
* http://LB_IP_ADDRESS:LB_PORT/sample.php - The Sample application  
* http://LB_IP_ADDRESS:LB_PORT/index.html - The Hangman game  

Usage example : 
 You can invoke the following custom command
 
 <strong>invoke mysql query root lampdb \\\"update persons set fname = \\\'Robinzon\\\' where id =1\\\"</strong>
 
 And then refresh http://LB_IP_ADDRESS:LB_PORT/sample.php to view the change.
 
## Known Issues

* <strong>apache recipe</strong>: If sendmail is installed on the VM, emails can be sent. The recipe doesn't install sendmail. 

* <strong>mysql recipe</strong>: 
 * monitoring is not implemented yet. It will be asap.  
 * This recipe installs standalone MySQL DB and not master-slave. MySQL master-slave will be available asap.  
 
 


