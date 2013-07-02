# XAP

**Status**: Tested  
**Description**: Xap recipe  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [kobi](https://github.com/kobikis)  
**License**:      Apache 2.0  
**Linux* sudoer permissions**:	required  
**Release Date**: July 2st 2013  


Tested on:
--------

* <strong>EC2</strong>: CentOs

Synopsis
--------

This folder contains a service recipe for the XAP application.  
The XAP application enable you to deploy XAP EDG or ProcessingUnits using Cloudify.  
The default XAP version is the latest stable one 9.5.2.  

This application contains 3 services:  
    **mgt** - this service is running single GridServiceManager and LookUp Service.  
    **pu** - this service is running is running GridServiceContainers (configured at pu-service.properties file)  
    **webui** - this service is running a XAP management console.  


Deployment
----------

 1. At <Cloudify Home>/clouds/ec2/upload/cloudify-overrides create a folder xap-license and copy a valid XAP license.
 2. You can deploy EDG or Processing Unit.
    If you want to deploy an EDG configure the following properties:  
        - isEDG=true  
        - gscCount="1"   -  number of GSCs in each pu service instance.  
        - dataGrids="mySpace1"  - data grid names  
        - numberOfPrimaries="1" - number of primaries  
        - numberOfsBackupsPerPrimary="1" - number of backups per primary.  

    If you want to deploy an Processing Unit  configure the following properties:  
        - isEDG=false  
        - gscCount="1"   -  number of GSCs in each pu service instance.  
        - puUrl="https://s3.amazonaws.com/example" - public url  
        - puJars="processor-3.0.0-SNAPSHOT.jar" - processing unit jar/war  
        - numberOfPrimaries="1" - number of primaries  
        - numberOfsBackupsPerPrimary="1" - number of backups per primary.  
 3. bootstrap-cloud ec2
 4. install-application xap
 5. You can open XAP management console by entering the public webui service ip on port 8099.



