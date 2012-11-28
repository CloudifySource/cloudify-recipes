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
import com.mongodb.CommandResult;
import com.mongodb.Mongo;
import com.mongodb.DB;
service {
	
	name "mongoConfig"
	icon "mongodb.png"
	type "NOSQL_DB"

  // number of mongo config instances must be 1 or 3
	numInstances 1
	
	compute {
		template "SMALL_LINUX"
	}

	lifecycle {
		install "mongoConfig_install.groovy"
		start "mongoConfig_start.groovy"		
		startDetectionTimeoutSecs 400
		startDetection {
			ServiceUtils.isPortOccupied(context.attributes.thisInstance["port"])
		}
		
		monitors{
			try { 
				port  = context.attributes.thisInstance["port"] as int
				mongo = new Mongo("127.0.0.1", port)			
				db = mongo.getDB("mydb")
														
				result = db.command("serverStatus")
				println "mongod-service.groovy: result is ${result}"	
														
				return [
					"Active Read Clients":result.globalLock.activeClients.readers,
					"Active Write Clients":result.globalLock.activeClients.writers, 
					"Read Clients Waiting":result.globalLock.currentQueue.readers, 
					"Write Clients Waiting":result.globalLock.currentQueue.writers, 
					"Current Active Connections":result.connections.current,
					"Open Cursors":result.cursors.totalOpen
				]
			}			
			finally {
				if (null!=mongo) mongo.close()
			}
			
			
		}		
	}
		
	userInterface {
		metricGroups = ([
			metricGroup {
				name "MongoDB"
				metrics([
					"Open Cursors", 
					"Current Active Connections",
					"Active Read Clients",
					"Active Write Clients",
					"Read Clients Waiting",
					"Write Clients Waiting"					
				])
			}
		])

		widgetGroups = ([
			widgetGroup {
				name "Open Cursors"
				widgets ([
					balanceGauge{metric = "Open Cursors"},
					barLineChart{
						metric "Open Cursors"
						axisYUnit Unit.REGULAR
					},
				])
			}, 
			widgetGroup {
				name "Current Active Connections"
				widgets ([
					balanceGauge{metric = "Current Active Connections"},
					barLineChart{
						metric "Current Active Connections"
						axisYUnit Unit.REGULAR
					},
				])
			}, 			
			widgetGroup {
				name "Active Read Clients"
				widgets ([
					balanceGauge{metric = "Active Read Clients"},
					barLineChart{
						metric "Active Read Clients"
						axisYUnit Unit.REGULAR
					},
				])
			}, 
			widgetGroup {
				name "Active Write Clients"
				widgets ([
					balanceGauge{metric = "Active Write Clients"},
					barLineChart{
						metric "Active Write Clients"
						axisYUnit Unit.REGULAR
					},
				])
			}, 
			widgetGroup {
				name "Read Clients Waiting"
				widgets ([
					balanceGauge{metric = "Read Clients Waiting"},
					barLineChart{
						metric "Read Clients Waiting"
						axisYUnit Unit.REGULAR
					},
				])
			}, 
		    widgetGroup {
				name "Write Clients Waiting"
				widgets ([
					balanceGauge{metric = "Write Clients Waiting"},
					barLineChart{
							metric "Write Clients Waiting"
							axisYUnit Unit.REGULAR
					},
				])
			}    
		])
	}  
}
