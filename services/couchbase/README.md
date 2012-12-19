# Couchbase

**Status**: Tested  
**Description**: Couchbase  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**Demo**: [cloudifysourcetv](http://www.cloudifysource.org/cloudifysourcetv#N8V44NoVhKM)   
**License**:      Apache 2.0   
**Build**:   [Cloudify 2.3.0 RC](http://repository.cloudifysource.org/org/cloudifysource/2.3.0-RC/gigaspaces-cloudify-2.3.0-rc-b3483.zip)   
**Linux* sudoer permissions**:	Mandatory  
**Windows* Admin permissions**:  Not required    
**Release Date**: December 19th 2012  


Tested on:
--------

* <strong>EC2</strong>: 
 * <strong>CentOs 5</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"   
 * <strong>Ubuntu 12.04</strong>: imageId "us-east-1/ami-82fa58eb", hardwareId "m1.small", locationId "us-east-1"  
.
* <strong>OpenStack</strong>:  
 * <strong>CentOs 5</strong>: imageId "1234" CentOS 5.6 64-bit, hardwareId "103"  standard.large - 4 vCPU / 8 GB RAM / 240 GB HD , az-1.region-a.geo-1 


Synopsis
--------

This folder contains a service recipe for Couchbase.


> *Important*: <strong>In order to use this recipe, the installing user must be a sudoer in the installed VMs.</strong>


## Known Issues

Currently, Couchbase does not support running more than one instance per OS/VM since the basic Couchbase RPM package does not support [relocation](http://rpm5.org/docs/api/relocatable.html).


