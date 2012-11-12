# Drupal 

**Status**: Tested  
**Description**:  A recipe for installing Drupal version 6 or 7  
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**Demo**: 1. Drupal:TBD  2. [MySQL](http://www.cloudifysource.org/cloudifysourcetv#6Li_lCZXaKY)    
**License**:      Apache 2.0   
**Build**:   [Cloudify 2.2.0 GA](http://repository.cloudifysource.org/org/cloudifysource/2.2.0-RELEASE/gigaspaces-cloudify-2.2.0-ga-b2500)     
**Linux* sudoer permissions**:	Mandatory  
**Release Date**: Nov 4th 2012    


Tested on:
--------

* <strong>EC2</strong>: 
 * <strong>CentOs 5</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"  
 * <strong>Ubuntu 11.10</strong>: "us-east-1/ami-e1aa7388", hardwareId "m1.small", locationId "us-east-1"   
.
* <strong>OpenStack</strong>:  
 * <strong>CentOs 5</strong>: imageId "1234" CentOS 5.6 64-bit, hardwareId "103"  standard.large - 4 vCPU / 8 GB RAM / 240 GB HD , az-1.region-a.geo-1 


Synopsis
--------

This folder contains a service recipe for [Drupal](http://drupal.org/) which is an open source content management platform powering millions of websites and applications.

This application recipe enables users to install Drupal (version 6 or 7) on any cloud.

After the installation it enables users to update modules,themes and files without accessing the Cloud VMs.

Users can delete the site's cache, put the site in maintenance mode and also bring it back to "life" (online status) from the CLI, without accessing the Cloud VMs and without accessing the site (in the browser).

This recipe is comprised of two services : 
* [drupal](drupal/drupal-service.groovy) which inherits from the base [apache](../../services/apache/README.md) service and extends it.  
* [mysql](../../services/mysql/README.md) which inherits from the base mysql service. 



## Custom Commands 

**A) cmd**: ( for the drupal service )

	This custom command enables users to upload a module ,theme or file to their Drupal site.
	Usage :  
		invoke drupal cmd upload [moudle|theme] zip_full_url_path
		  or
		invoke drupal cmd upload file file_full_url_path  destination_folder_relative_to_home
	Examples: 
		invoke drupal cmd upload module http://ftp.drupal.org/files/projects/views-7.x-3.5.zip
		invoke drupal cmd upload module http://my1stStorageSite.com/myNewModule-7.x-1.4.zip
		invoke drupal cmd upload theme http://ftp.drupal.org/files/projects/sasson-7.x-2.7.zip
		invoke drupal cmd upload theme http://my2ndStorageSite.com/myNewTheme-7.x-2.2.zip
		invoke drupal cmd upload file http://my3rdStoragSite.com/myFile sites/default/files/pictures
	
**B) drupalCommand**: ( for the mysql service ) 

	This custom command enables users to invoke a Drupal command on the (MySQL) database.
	Usage :   invoke mysql drupalCommand currentCommandName	
	
	  # The following brings the Drupal site back to "life" (i.e. : to online status) 
	  Example1: invoke mysql drupalCommand activateSite
		
	  # The following puts the Drupal site in maintenance mode:  
	  Example2: invoke mysql drupalCommand siteOffline 
	
	  # The following deletes the Drupal site's cache: 
	  Example3: invoke mysql drupalCommand deleteCache 
	
	