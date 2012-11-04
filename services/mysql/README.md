# MySQL 

**Status**: Tested  
**Description**:  MySQL   
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**Demo**: [cloudifysourcetv](http://www.cloudifysource.org/cloudifysourcetv#6Li_lCZXaKY)   
**License**:      Apache 2.0   
**Build**:   [Cloudify 2.1.1 GA](http://repository.cloudifysource.org/org/cloudifysource/2.1.1/gigaspaces-cloudify-2.1.1-ga-b1400.zip) , [Cloudify 2.2.0 M2](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m2-b2491.zip) and 
[Cloudify 2.2.0 M4](http://repository.cloudifysource.org/org/cloudifysource/2.2.0/gigaspaces-cloudify-2.2.0-m4-b2493-77.zip)     
**Linux* sudoer permissions**:	Mandatory  
**Windows* Admin permissions**:  Not required    
**Release Date**: September 24th 2012  


Tested on:
--------

* <strong>EC2</strong>: 
 * <strong>CentOs 5</strong> imageId "us-east-1/ami-76f0061f", hardwareId "m1.small", locationId "us-east-1"  
 * <strong>Ubuntu 11.10</strong>: "us-east-1/ami-e1aa7388", hardwareId "m1.small", locationId "us-east-1"  
 * <strong>Ubuntu 12.04</strong>: imageId "us-east-1/ami-82fa58eb", hardwareId "m1.small", locationId "us-east-1"  
.
* <strong>OpenStack</strong>:  
 * <strong>CentOs 5</strong>: imageId "1234" CentOS 5.6 64-bit, hardwareId "103"  standard.large - 4 vCPU / 8 GB RAM / 240 GB HD , az-1.region-a.geo-1 
.
* <strong>Rackspace</strong>: 
 * <strong>CentOs 6</strong>: imageId "118", hardwareId "4" (2GB server  = 2G RAM 80 GB HD). 

 We disable the requiretty flag in /etc/sudoers on the installed VMs, so that Cloudify will be able to invoke remote ssh commands as a sudoer. This feature will be a part of Cloudify in the near future.
Until then, please use the [Cloud Drivers Repository](https://github.com/CloudifySource/cloudify-cloud-drivers).



Synopsis
--------

This folder contains a service recipe for MySQL.

This recipe enables users to install MySQL in two modes: standalone and master-slave.

In a master-slave mode, the first instance of the service is the master and all the other instances are slaves.

In order to work in a master-slave mode, you need to set the masterSlaveMode property to true in mysql-service.properties file.

The default port is 3306, but it can be modified in the mysql-service.properties file.

You can inherit and extend this recipe very easily, just by changing the mysql-service.properties file, without changing even one line of code in the recipe.

This is achieved thanks to the following  : 

1:	<strong>A Start Detection Query</strong> 

In most of our recipes, we use something like : <strong>ServiceUtils.isPortOccupied(port)</strong>.  
In this case, it is usually NOT enough, because the port just means that the DB is up, but we also need the schema to be ready.  
So we added a <strong>startDetectionQuery</strong> property (to the properties file) in which you can insert an SQL query.  
The service instance is alive only if the port is occupied AND the startDetectionQuery result is true.

2:	<strong>Post Start Actions</strong>

In the properties file you can insert an array of postStart commands (as many commands as you want).  
These post start commands will be invoked during the... postStart lifecycle event.    
There are four types of postStart commands :  
**a) mysqladmin** : for invoking any administrative command ( for example : creating a new DB )   
**b) mysql**      : for invoking any SQL statement: insert, update, grant permissions etc.  
**c) import**     : for importing a DB schema ( by providing a full URL of the zip file that contains the schema )  
**d) mysqldump**  : for creating a db dump (snapshot)  

Examples :  
   
<pre><code>   
   /* In this case, dbName is a property which is defined in mysql-service.properties    
      a) All the occurrences of MYSQLHOST in actionQuery, 
	  b) will be replaced with the private IP address on which this service instance resides.   
   */ 
   [    
		"actionType" : "mysqladmin",   
		"actionQuery" : "create",  
		"actionUser"  : "root",  
		"actionDbName" : "${dbName}",  
		"debugMsg" : "Creating db - Name  : ${dbName} ... "  
	]  
</code></pre>  

<pre><code>	
	/* In this case, dbUser and dbPassW are properties which are defined in mysql-service.properties   
       a) All the occurrences of MYSQLHOST in actionQuery, 
	   b) will be replaced with the private IP address on which this service instance resides.   
	*/  
	[   
		"actionType" : "mysql", 		  
		"actionQuery" : "\"CREATE USER '${dbUser}'@'localhost' IDENTIFIED BY '${dbPassW}';\"",  
		"actionUser"  : "root",  
		"actionDbName" : "${dbName}",  
		"debugMsg" : "Creating db user ${dbUser} at localhost, passw ${dbPassW} in ${dbName} db... "   
	]  
</code></pre>  	
<pre><code>	  
   /* In this case:  
        a) dbName,currDBZip,currImportSql are properties which are defined in mysql-service.properties   
        b) currDBZip is the local name of the zip file ( after download )  
        c) currImportSql is the name of the sql file which is stored in currDBZip.   
        d) All the occurrences of REPLACE_WITH_DB_NAME in currImportSql, will be replaced with ${dbName}   
   */
   [   
		"actionType" : "import",   
		"importedZip" : "${currDBZip}",  
		"importedFile" : "${currImportSql}",  
		"importedFileUrl" : "http://dropbox/1/222/mysql.zip",  
		"actionUser"  : "root",  
		"actionDbName" : "${dbName}",  
		"debugMsg" : "Importing  to ${dbName} ..."  
	]	
</code></pre>	
<pre><code>	
   /* In this case:  
        a) dbName is a property which is defined in mysql-service.properties.  
        b) If actionDbName is an empty string,  then --all-databases will be used. 
        c) actionArgs contain the flags that you want to use with this mysqldump command  
        d) Do NOT database flags, because they will be set according to the actionDbName.  
           i.e: Do NOT use the following  : --all-databases,-A,--databases  
        e) Do NOT -u flag flags, because it will be set according to the actionUser  
   */
   [   
		"actionType" : "mysqldump",   
		"actionArgs" : "--add-drop-database -c --lock-all-tables -F",  
		"actionUser"  : "root",  
		"actionDbName" : "${dbName}",  
		"dumpPrefix" : "myDumpFile_",  
		"debugMsg" : "Invoking mysqldump ..."   
	]	
</code></pre>


3:	<strong>my.cnf variables Replacement</strong> 

In the properties file you can insert an array of my.cnf variables and their corresponding values (as many as you want).  
Do NOT set the server-id variable, as it will be set by this recipe.

These variables will be replaced during the install lifecycle event.
   
Examples :  
   
<pre><code>   
   /* In this case, the value of the log-bin, 
      will be set to mysql-bin in the mysqld section of my.cnf.
   */      
	[  
		"section" : "mysqld", 
		"variable" : "log-bin" ,
		"newValue"  : "mysql-bin"
	], 

   /* In this case, the value of the binlog_format   , 
      will be set to MIXED (ROW and STATEMENT) in the mysqld section of my.cnf.
   */ 	
	
	[  
		"section" : "mysqld", 
		"variable" : "binlog_format" ,
		"newValue"  : "MIXED"
	]

</code></pre>
	
   

## Custom Commands 

**A) mysqldump**:   

This custom command enables users to create a database snapshot (mysqldump).  
Usage :  <strong>invoke mysql mysqldump actionUser dumpPrefix [dbName]</strong>    
Example: <strong>invoke mysql mysqldump root myPrefix_ myDbName</strong>  
	
	
**B) query**:  
 
This custom command enables users to invoke an SQL statement.  
Usage :  <strong>invoke mysql query actionUser [puserPassword] dbName query</strong>  
Examples: 

1. If you want to update the users table in myDbName with the following statement :    
<strong>update users set name='James' where uid=1</strong>   
   then you need to run the following custom command :   
<strong>invoke mysql query root myDbName \\\"update users set name=\\\'James\\\' where uid=1\\\"</strong>  
or
<strong>invoke mysql query myUser pmyUserpassword myDbName \\\"update users set name=\\\'James\\\' where uid=1\\\"</strong>  

2. If you want to insert a new user named Dan, into the users table in myDbName, and you need the following SQL statement:  
<strong>INSERT INTO users VALUES (17,'Dan','hisPassword','hisemail@his.com',0)</strong>  
   then you need to run the following custom command :   
<strong>invoke mysql query root tamirDB \\\"INSERT INTO users VALUES \\\\(17,\\\'Dan\\\',\\\'hisPassword\\\',\\\'hisemail@his.com\\\',0\\\\)\\\"</strong>  


**C) import**:

This custom command enables users to import a zipped file to a database
Usage :  <strong>invoke mysql import actionUser dbName zipFileURL</strong>
Example: <strong>invoke mysql import root myDbName http://www.mysite.com/myFile.zip</strong>


**D) addSlave**:

This custom command enables users to add a slave to the master.
It should be invoked only on a master instance (by a remote slave) 
and only if masterSlaveMode is set to true on both the slave and master.
As a result, the following will be invoked :  
mysql -u root -D dbName -e "GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO slaveUser@'slaveHostIP' IDENTIFIED BY 'slavePassword';"

Usage :  <strong>invoke mysqlmaster addSlave actionUser dbName slaveUser slavePassword slaveHostIP</strong>
			
			
**E) showMasterStatus**:	
					
This custom command enables users to show the master's status.
It should be invoked only on a master instance (either by the master or by a remote slave) 
and only if masterSlaveMode is set to true.
As a result, the following will be invoked :  
mysql -u root -D dbName -e "show master status;" 
and then the mysql-bin will be stored in context.attributes.thisApplication["masterBinLogFile"] 
and the master's log's position will be stored in context.attributes.thisApplication["masterBinLogPos"]  
		
Usage :  <strong>invoke mysqlmaster showMasterStatus actionUser dbName</strong>


## Known Issues

 * Monitoring is not implemented yet. It will be available in the future.  
 * Failover in the master-slave mode is NOT implemented yet. It will be available in the future.  
 
 