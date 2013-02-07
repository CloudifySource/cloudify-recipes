# masterslave 

**Status**: Tested  
**Description**: MySQL master-slave Sample Application  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:  [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**Demo**: [cloudifysourcetv](http://www.cloudifysource.org/cloudifysourcetv#6Li_lCZXaKY)   
**License**:      Apache 2.0   
**Build**:  [Cloudify 2.2.0 M4](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m4-b2493-77.zip)   
**Linux* sudoer permissions**:	Mandatory     
**Release Date**: September 24th 2012  


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

This folder contains a sample application recipe for MySQL master-slave.

This recipe contains (inherits) the **[mysql](../../services/mysql/README.md)** recipe.

The first service instance becomes the master (automatically) and the other instances (two instances in our case), are the slaves.

Usage example : 
 Invoke the following custom commands:
 
 <strong>1. invoke mysql query root lampdb \\\"select * from persons\\\"</strong>
   
   As a result, you will see that the content of the persons table in all three instances is identical.
 
 <strong>2. invoke -instanceid 1 mysql query root lampdb \\\"update persons set fname = \\\'mynewFamilyName\\\' where id =1\\\"</strong>
   
   This will update the persons table only in the database of the master.
   
 <strong>3. invoke mysql query root lampdb \\\"select * from persons\\\"</strong>
   
   As a result, you will see that (again) the content of the persons table in all three instances is identical, 
   although we invoked the previous command(#2) only on the master instance(-instanceid 1).
 
 <strong>4. invoke -instanceid 1 mysql import root lampdb http://repository.cloudifysource.org/org/cloudifysource/examples/lamp/1.0.0/newSample.zip</strong>
   
   The above will import data to the persons table only in the database of the master. 
 
 <strong>5. invoke mysql query root lampdb \\\"select * from persons\\\"</strong>
   
   As a result, you will see that (one again) the content of the persons table in all three instances is identical, 
   although we invoked the previous command(#4) only on the master instance(-instanceid 1).

 
## Known Issues

 * Monitoring is not implemented yet. It will be available in the future.  
 * Failover in the master-slave mode is NOT implemented yet. It will be available in the future.  
 


