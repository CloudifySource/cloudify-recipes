# Apache

**Status**: Tested  
**Description**: Apache / Apache PHP  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**:   [Cloudify 2.2.0 M4](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m4-b2493-77.zip)   
**Linux* sudoer permissions**:	Mandatory  
**Windows* Admin permissions**:  Not required    
**Release Date**: August 31st 2012  


Tested on:
--------

* <strong>EC2</strong>: 
 * <strong>CentOs 5</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"  
 * <strong>Ubuntu 11.10</strong>: "us-east-1/ami-e1aa7388", hardwareId "m1.small", locationId "us-east-1"  
 * <strong>Ubuntu 12.04</strong>: imageId "us-east-1/ami-82fa58eb", hardwareId "m1.small", locationId "us-east-1"  
.
* <strong>OpenStack</strong>:  
 * <strong>CentOs 5</strong>: imageId "1234" CentOS 5.6 64-bit, hardwareId "103"  standard.large - 4 vCPU / 8 GB RAM / 240 GB HD , az-1.region-a.geo-1 


Synopsis
--------

This folder contains a service recipe for Apache HTTP Server. 
The apache server can be configured to support PHP by changing one property in the properties file.
It can be configured to use and connect to MySQL by changing one property in the properties file (The MySQL database which the apache connects to, can be either installed by Cloudify or external).

Many attributes can be also be set in the play-service.properties.

Here are a few of them:

**php**: By modifying the php to "true", you can enable the apache server to support PHP.  

**port**: Its default port is 8080, but it can be modified in the apache-service.properties. 

**Context Path**: By modifying the ctxPath, you can set the context path of your application. 

Your deployed application can then be accessed at [http://DEPLOYED_APPLICATION_IP_ADDRESS:port/ctxPath](http://DEPLOYED_APPLICATION_IP_ADDRESS:port/ctxPath) .

**Database**: 

Set the **dbServiceName** property ONLY if your application uses a db.

Example:

For MySQL, use the following: **dbServiceName="mysql"** . 

For PostgreSQL (not supported , so u need to implement it yourself) , use the following: **dbServiceName="postgresql"**
	
If your application doesn't required a db, leave this property as is (NO_DB_REQUIRED) or remove it.

Set the **dbHost** property ONLY if your application uses a db which is NOT installed by Cloudify, 
   otherwise leave is (DB_INSTALLED_BY_CLOUDIFY) or remove it.
   If the db is installed by Cloudify, then Cloudify will calculate it.
   This property is used only if you set the dbServiceName property.

Set the **dbPort** property ONLY if your application uses a db which is NOT installed by Cloudify, 
   otherwise leave is (DB_INSTALLED_BY_CLOUDIFY) or remove it.
   If the db is installed by Cloudify, then Cloudify will calculate it.
   This property is used only if you set the dbServiceName property.

Set the **applyEvolutions** property ONLY if your application uses a db and you want the play framework to... apply your database's evolutions(1.sql, 2.sql etc.)
   This property is used only if you set the dbServiceName property.


> *Important*: <strong>In order to use this recipe, the installing user must be a sudoer in the installed VMs.</strong>


## Known Issues

If sendmail is installed on the VM, emails can be sent. The recipe doesn't install sendmail.

