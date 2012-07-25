/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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
import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.sql.*
import org.cloudifysource.dsl.utils.ServiceUtils;
import com.mysql.jdbc.Driver

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())

context = ServiceContextFactory.getServiceContext()

println "mysql_startDetection.groovy: jdbcPort is ${config.jdbcPort} ..."
if ( ServiceUtils.isPortOccupied(config.jdbcPort) ) { 				
	println "mysql_startDetection: port ${config.jdbcPort} is now occupied ..."	
	if ( config.startDetectionQuery.length() == 0  ) {
		println "mysql_startDetection: startDetectionQuery is empty - service is up "
		System.exit(0)
	}
	try {	
		def mysqlHost
		if (  context.isLocalCloud()  ) {
			mysqlHost =InetAddress.getLocalHost().getHostAddress()
		}
		else {
			mysqlHost =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
		}	
		def connUrl="jdbc:mysql://${mysqlHost}:${config.jdbcPort}/${config.dbName}"
		println "mysql_startDetection: connUrl is ${connUrl}"
		def sql = Sql.newInstance("${connUrl}", "${config.dbUser}","${config.dbPassW}", "com.mysql.jdbc.Driver")
		println "mysql_startDetection: ran query ${config.startDetectionQuery}"
		rows = sql.rows(config.startDetectionQuery)
		println "mysql_startDetection: rows value is ${rows}"
		if ( rows.size > 0 ) {
			println "mysql_startDetection: Rows size " + rows.size
			def row0 = rows[0]
			println "mysql_startDetection: row0 is ${row0}"
			if ( row0 != null ) {
				def cc = row0["cc"] as int
				context.attributes.thisInstance["cc"] = cc
				println "mysql_startDetection: Count " + cc				
				//return  cc > 0 
				if ( cc > 0 ) {
					System.exit(0)
				}				
				else {
					System.exit(-1);
				}
			}
			println "mysql_startDetection: row0 is null"
		}	
		println "mysql_startDetection: rows.size is zero"	
		
	}
	catch (Exception e) {
		//println e.getMessage()
		System.exit(-1);
	} 
		
}
//return false;
System.exit(-1);