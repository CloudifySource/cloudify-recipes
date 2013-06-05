/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import groovy.sql.*
import com.mysql.jdbc.Driver

service {
	
	name "mysql"

	icon "mysql.png"
	type "DATABASE"
	elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 3	
	
	compute {
		template "SMALL_LINUX"
	}	
	
	lifecycle{
 
		install "mysql_install.groovy"

		start "mysql_start.groovy"
		
		startDetectionTimeoutSecs 900
		startDetection "mysql_startDetection.groovy"
		
		stopDetection {	
			!ServiceUtils.isPortOccupied(jdbcPort)
		}
		
		preStop ([	
			"Win.*":"killAllMysql.bat",		
			"Linux.*":"mysql_stop.groovy"
			])
		shutdown ([			
			"Linux.*":"mysql_uninstall.groovy"
		])
			
			
		locator {	
			//hack to avoid monitoring started processes by cloudify
			  //return  [] as LinkedList	
			 
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.re=mysql.*\\.exe|mysqld")
			println ":mysql-service.groovy: current PIDs: ${myPids}"
			return myPids
		}
		
		details {
			def currPublicIP
			
			if ( context.isLocalCloud() ) {
				currPublicIP = InetAddress.localHost.hostAddress	
			}
			else {
				currPublicIP =context.getPublicAddress()	
			}
			return [	
				"MySQL IP":currPublicIP,
				"MySQL Port":jdbcPort
			]
		}	
	}
	
	customCommands ([
	/* 
	This custom command enables users to create a database snapshot (mysqldump) 
	 and to upload the backup to an external storage (Amazon S3 for example).
	Usage :  invoke mysql mysqldump actionUser dumpPrefix dbName backupType bucketName
	Example: invoke mysql mysqldump root myPrefix_ myDbName s3 myBucketName
	*/
	
		"mysqldump" : "mysql_dump.groovy" , 
			
		/* 
			This custom command enables users to invoke an SQL statement
			Usage :  invoke mysql query actionUser [puserPassword] dbName query
			Examples: 			
				1. invoke mysql query root myDbName "update users set city=\"NY\" where uid=15"
				2. invoke mysql query root pmyRootPassword myDbName "update users set city=\"NY\" where uid=15"			
			
		*/			
		"query" : "mysql_query.groovy" ,
		
		/* 
			This custom command enables users to add a slave to the master.
	        It should be invoked only on a master instance (by a remote slave) 
			and only if masterSlaveMode is set to true on both the slave and master.
	        As a result, the following will be invoked :  
	        mysql -u root -D dbName -e 
			  "GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO slaveUser@'slaveHostIP' IDENTIFIED BY 'slavePassword';"
	
	       Usage :  invoke mysqlmaster addSlave actionUser dbName slaveUser slavePassword slaveHostIP 			
			
		*/
		"addSlave": "mysql_addSlave.groovy" , 
		
		/* 
			This custom command enables users to show the master's status.
	        It should be invoked only on a master instance (either by the master or by a remote slave) 
			and only if masterSlaveMode is set to true.
	        As a result, the following will be invoked :  
	        mysql -u root -D dbName -e "show master status;" 
		    and the mysql-bin will be stored in context.attributes.thisApplication["masterBinLogFile"] 
		    and the master's log's position will be stored in context.attributes.thisApplication["masterBinLogPos"]  
		
	       Usage :  invoke mysqlmaster showMasterStatus actionUser dbName  			
			
		*/
		"showMasterStatus": "mysql_showMasterStatus.groovy" , 
		
		/* 
			This custom command enables users to import a zipped file to a database
			Usage :  invoke mysql import actionUser dbName zipFileURL
			Example: invoke mysql import root myDbName http://www.mysite.com/myFile.zip
		*/
		
		"import" : "mysql_import.groovy"
	])
	

	userInterface {
		metricGroups = ([
			metricGroup {
				name "server"

				metrics([
					"Server Uptime",
					"Client Connections",
					"Total Queries",
					"Slow Queries",
					"Opens",
					"Current Open Tables",
					"Queries Per Second"
				])
			}
		])

		widgetGroups = ([
			widgetGroup {
           			name "Server Uptime"
            		widgets ([
               		barLineChart{
                  		metric "Server Uptime"
                  		axisYUnit Unit.REGULAR
							},
            		])
						
         }   , 
			widgetGroup {
           			name "Client Connections"
            		widgets ([
               		barLineChart{
                  		metric "Client Connections"
                  		axisYUnit Unit.REGULAR
							},
            		])
						
         }   , 
			widgetGroup {
           			name "Total Queries"
            		widgets ([
               		barLineChart{
                  		metric "Total Queries"
                  		axisYUnit Unit.REGULAR
							},
            		])
						
         }   , 
			widgetGroup {
           			name "Opens"
            		widgets ([
               		barLineChart{
                  		metric "Opens"
                  		axisYUnit Unit.REGULAR
							},
            		])
						
         }   , 
			widgetGroup {
           			name "Current Open Tables"
            		widgets ([
               		barLineChart{
                  		metric "Current Open Tables"
                  		axisYUnit Unit.REGULAR
							},
            		])
						
         }   , 
			widgetGroup {
           			name "Queries Per Second"
            		widgets ([
               		barLineChart{
                  		metric "Queries Per Second"
                  		axisYUnit Unit.REGULAR
							},
            		])
						
         }   , 
		])
	}  
}
