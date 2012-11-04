# Computer database

**Status**: Tested  
**Description**: Computer database  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:  [Adam Lavie](https://github.com/adaml) , [Eitan Yanovsky](https://github.com/eitany) and [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**Demo**: [cloudifysourcetv](http://www.cloudifysource.org/cloudifysourcetv#6Li_lCZXaKY)   
**License**:      Apache 2.0   
**Build**:  [Cloudify 2.2.0 M2](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m2-b2491.zip) and [Cloudify 2.2.0 M4](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m4-b2493-77.zip)   
**Linux* sudoer permissions**:	Mandatory     
**Release Date**: August 10th 2012  


Tested on:
--------

* <strong>EC2</strong>: CentOs 
* <strong>OpenStack</strong>: CentOs 

We disable the requiretty flag in /etc/sudoers on the installed VMs, so that Cloudify will be able to invoke remote ssh commands as a sudoer. This feature will be a part of Cloudify in the near future.
Until then, please use the [Cloud Drivers Repository](https://github.com/CloudifySource/cloudify-cloud-drivers).


Synopsis
--------

This folder contains a service recipe for the [Computer database](http://www.playframework.org/documentation/2.0.1/Samples) Play framework Sample application.

The Computer database is a classic CRUD application, backed by a JDBC database. It demonstrates:

* accessing a JDBC database, using Ebean in Java and Anorm in Scala
* table pagination and CRUD forms
* integrating with a CSS framework


This recipe is comprised of three services:
* [play](../../services/play/README.md) 
* [mysql](../../services/mysql/README.md) 
* [apacheLB](../../services/apacheLB/README.md) 


