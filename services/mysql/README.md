# MySQL 

**Status**: Tested  
**Description**:  MySQL   
**Maintainer**:       Cloudify  
**Maintainer email**: cloudifysource@gigaspaces.com  
**Contributors**:    [tamirko](https://github.com/tamirko)  
**Homepage**:   [http://www.cloudifysource.org](http://www.cloudifysource.org)  
**License**:      Apache 2.0   
**Build**:  [2.1.1 GA](http://repository.cloudifysource.org/org/cloudifysource/2.1.1/gigaspaces-cloudify-2.1.1-ga-b1400.zip)   
**Linux* sudoer permissions**:	Mandatory  
**Windows* Admin permissions**:  Not required    
**Release Date**: July 25th 2012  


Tested on:
--------

* <strong>EC2</strong>: Ubuntu and CentOs 
* <strong>OpenStack</strong>: CentOs 
* <strong>Rackspace</strong>: CentOs 



Synopsis
--------

This folder contains a service recipe for MySQL.

Its default port is 3306, but it can be modified in the mysql-service.properties.

You can inherit and extend this recipe very easily, just by changing the mysql-service.properties file, without changing even one line of code in the recipe.

This is achieved thanks to the following  : 

1.	A Start Detection Query

In most of our recipes, we use something like : ServiceUtils.isPortOccupied(port).
In this case, it is usually NOT enough, because the port just means that the DB is up, but we also need the schema to be ready.
So we added a <strong>startDetectionQuery</strong> property (to the properties file) in which you can insert an SQL query.
The service instance is alive only if the port is occupied AND the startDetectionQuery result is true.

2.	Post Start Actions

In the properties file you can insert an array of postStart commands (as many commands as you want).
These post start commands will be invoked during the... postStart lifecycle event.
There are four types of postStart commands :  
**a)	<strong>mysqladmin</strong>: for invoking any administrative command ( for example : creating a new DB )   
**b)	<strong>mysql</strong> : for invoking any SQL statement: insert, update, grant permissions etc.  
**c)	<strong>import</strong> : for importing a DB schema ( by providing a full URL of the zip file that contains the schema )  
**d)  <strong>mysqldump</strong> : for creating a db dump (snapshot)  

ActionType can be one of the four following: mysqladmin,mysql,mysqldump or import
   Examples :
   
   // In this case, dbName is a property which is defined in this properties file 
   // All the occurrences of MYSQLHOST in actionQuery, will be replaced with the private IP address on which this service instance resides   
   [  
		"actionType" : "mysqladmin", 
		"actionQuery" : "create" ,
		"actionUser"  : "root",
		"actionDbName" : "${dbName}",
		"debugMsg" : "Creating db - Name  : ${dbName} ... "
	] ,
	
	// In this case, dbUser and dbPassW are properties which are defined in this properties file 
   // All the occurrences of MYSQLHOST in actionQuery, will be replaced with the private IP address on which this service instance resides	
	[ 
		"actionType" : "mysql", 		
		"actionQuery" : "\"CREATE USER '${dbUser}'@'localhost' IDENTIFIED BY '${dbPassW}';\"",
		"actionUser"  : "root",
		"actionDbName" : "${dbName}",
		"debugMsg" : "Creating db user ${dbUser} at localhost, passw ${dbPassW} in ${dbName} db... " 
	],
   
   // In this case:
   //    dbName,currDBZip,currImportSql are properties which are defined in this properties file 
   //    currDBZip is the local name of the zip file ( after download )  
   //    currImportSql is the name of the sql file which is stored in currDBZip. 
   /     All the occurrences of REPLACE_WITH_DB_NAME in currImportSql, will be replaced with ${dbName}
   [ 
		"actionType" : "import", 
		"importedZip" : "${currDBZip}",
		"importedFile" : "${currImportSql}",
		"importedFileUrl" : "http://dropbox/1/222/mysql.zip",
		"actionUser"  : "root",
		"actionDbName" : "${dbName}",
		"debugMsg" : "Importing  to ${dbName} ..."
	]	
	
   // In this case:
   //    dbName is a property which is defined in this properties file.
   //    if actionDbName is an empty string,  then --all-databases will be used
   //    actionArgs contain the flags that you want to use with this mysqldump command
   //    Do NOT database flags, because they will be set according to the actionDbName.
   //    So do NOT use the following  : --all-databases,-A,--databases
   //    Do NOT -u flag flags, because it will be set according to the actionUser
   [ 
		"actionType" : "mysqldump", 
		"actionArgs" : "--add-drop-database -c --lock-all-tables -F",
		"actionUser"  : "root",
		"actionDbName" : "${dbName}",
		"dumpPrefix" : "myDumpFile_",
		"debugMsg" : "Invoking mysqldump ..." 
	]	
   



## Custom Commands 

#mysqldump - This custom command enables users to create a database snapshot (mysqldump).

Usage :  <strong>invoke mysql mysqldump actionUser dumpPrefix [dbName]</strong>
Example: <strong>invoke mysql mysqldump root myPrefix_ myDbName</strong>
		
#query - This custom command enables users to invoke an SQL statement.

Usage :  <strong>invoke mysql query actionUser dbName query</strong>
		
Examples 

1. If you want to update the users table in myDbName with the following statement : 
<strong>update users set name='James' where uid=1</strong>
   - then you need to run the following custom command : 
<strong>invoke mysql query root myDbName \\"update users set name=\\'James\\' where uid=1\\"</strong>

2. If you want to insert a new user named Dan, into the users table in myDbName, and you need the following SQL statement:
<strong>INSERT INTO users VALUES (17,'Dan','hisPassword','hisemail@his.com',0)</strong>
  - then you need to run the following custom command : 
<strong>invoke mysql query root tamirDB \\"INSERT INTO users VALUES \\(17,\\'Dan\\',\\'hisPassword\\',\\'hisemail@his.com\\',0\\)\\"</strong>

