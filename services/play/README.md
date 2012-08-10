# Play Framework 

**Status**: Tested  
**Description**:  Play Framework   
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:  [Adam Lavie](https://github.com/adaml) , [Eitan Yanovsky](https://github.com/eitany) and [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**:  [Cloudify 2.2.0 M2](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m2-b2491.zip)   
**Linux* sudoer permissions**:	Not required    
**Release Date**: August 10th 2012  


Tested on:
--------

* <strong>EC2</strong>: CentOs 
* <strong>OpenStack</strong>: CentOs 


Synopsis
--------

This folder contains a service recipe for the Play Framework.

*Play framework* is an open source web application framework, written in Scala and Java, which follows the model-view-controller architectural pattern. 
It aims to optimize developer productivity by using convention over configuration, hot code reloading and display of errors in the browser.

The recipe's default port is 8080, but it can be modified in the play-service.properties.


Many attributes can be also be set in the play-service.properties.

Here are a few of them:

**Context Path**: By modifying the applicationCtxPath, you can set the context path of your application.

Your deployed application can then be accessed at [http://DEPLOYED_APPLICATION_IP_ADDRESS:port/applicationCtxPath](http://DEPLOYED_APPLICATION_IP_ADDRESS:port/applicationCtxPath) .

If you use a load balancer, you can access your application at [http://LB_IP_ADDRESS:LB_PORT/applicationCtxPath](http://LB_IP_ADDRESS:LB_PORT/applicationCtxPath).

Known issue : Currently, when you access your application via [http://LB_IP_ADDRESS:LB_PORT/applicationCtxPath](http://LB_IP_ADDRESS:LB_PORT/applicationCtxPath), you get an error "Action not found". 

Until we fix it, please use [http://DEPLOYED_APPLICATION_IP_ADDRESS:port/applicationCtxPath](http://DEPLOYED_APPLICATION_IP_ADDRESS:port/applicationCtxPath) for access. 

This doesn't prevent you from testing your application under load via the load balancer ([http://LB_IP_ADDRESS:LB_PORT/applicationCtxPath](http://LB_IP_ADDRESS:LB_PORT/applicationCtxPath)).
We are working on solving this problem.

**Production or dev**: If you set the productionMode to true, your application will be deployed in a prod mode, otherwise : dev. 

**Database**: 

Set the **dbServiceName** property ONLY if your application uses a db.

Example:

For MySQL , use the following: **dbServiceName="mysql"** . 

For PostgreSQL (not supported -so u need to implement it your self) , use the following: **dbServiceName="postgresql"**
	
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




## Custom Commands 

**replace** - which enables users to replace a string in a file (relative to play home folder).

Usage : <strong>invoke play replace all|first origString newString relativePath</strong> 

**cmd** - which enables users to invoke *any* Play framework command line and up to 3 arguments. 

Usage : <strong>invoke play cmd nameOfTheCommand [arg1] [arg2] [arg3]</strong> 

**updateApp** : - which enables users to update their application

Usage : <strong>invoke play updateApp http://www.mynewapplication.zip</strong> 


	