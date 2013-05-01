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
service {
	
	name serviceName
	icon "couchbase.png"
	type "NOSQL_DB"
	elastic true
	numInstances 2
	minAllowedInstances 1
	maxAllowedInstances 4

	compute {
		template "SMALL_LINUX"
	}
	
	def instanceID = context.instanceId
	def portIncrement =  context.isLocalCloud() ? instanceID-1 : 0			
	def currentPort = couchbasePort + portIncrement
	
		
	lifecycle {
	
	
		details {
			def currPublicIP =context.getPublicAddress()
				
			def hostAndPort="http://${currPublicIP}:${currentPort}"
			def applicationURL = "${hostAndPort}"
		
			def detailsMap = [
				"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>" , 
				"couchbaseUser" : couchbaseUser ,
				"couchbasePassword": couchbasePassword
			]
													
			return detailsMap
		}	
		 
		monitors { 			
			def currInstanceID = context.instanceId						
			def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
			def cbstats="${scriptsFolder}/cbstats.sh"			
			def clusterBucketName = "CloudifyCouchbase${currInstanceID}"			

			try { 						
				def metrics=[:]
				def process="${cbstats} localhost ${couchbaseStatsPort} ${clusterBucketName}".execute()
				process.in.splitEachLine(":") { k,v -> 					
					metrics.put(k,v)
					//println "k,v: ${k},${v}"
				}
											
				return metrics 					
			}
			catch(Throwable e) {
				println "throwable is " + e
				return [
					"bytes_read" : 0 , 
					"bytes_written" : 0 , 
					"mem_used" : 0 
				]
			}					
		}
				
		install "couchbase_install.groovy"
						
		start "couchbase_start.groovy"		
			
		startDetectionTimeoutSecs 800
		startDetection {			
			ServiceUtils.isPortOccupied(currentPort)
		}	
		
		preStop "couchbase_stop.groovy"
			
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.re=epmd|beam|memsup|cpu_sup|memcached|moxi")
			println "couchbase-service.groovy: current PIDs: ${myPids}"
			return myPids
        }				
	}

	customCommands ([
		/* 
			This custom command enables users to add a server (to the 1st server).
			Usage :  invoke couchbase addServer newServerHost newServerPort newServerUser newServerPassword
			
			
			Example: invoke couchbase addServer 1234.543.556.33 8097 admin mypassword
		*/
	
		"addServer" : "couchbase_addServer.groovy" , 
		
		
		/* 
			This custom command enables users to rebalance the Couchbase cluster
			Usage :  invoke couchbase rebalance 
		*/
	
		"rebalance" : "couchbase_rebalance.groovy" , 

		/* 
			This custom command enables users to load data into the Couchbase cluster
			Usage :  invoke couchbase loadData path_to_zipFile 
			Examples 
		      1. If file1.zip is located in http://www.myZips.com/file1.zip , use the following command :
					invoke couchbase loadData http://www.myZips.com/file1.zip 
			  2. If file1.zip is located in /tmp/file1.zip (on the remote machine), use the following command :			  
			        invoke couchbase loadData /tmp/file1.zip
		*/	
		"loadData" : "couchbase_loadData.groovy"
		
	])		
	
	network {
		port currentPort
		protocolDescription "HTTP"
	}
	
	userInterface {

		metricGroups = ([
			metricGroup {

				name "cbStats"

				metrics([
				    "bytes_read",
					"bytes_written",
					"mem_used",
					"ep_mem_low_wat",
					"ep_mem_high_wat"
				])
			}
		])

		widgetGroups = ([
			widgetGroup {
				name "bytes_read"
				widgets ([
					balanceGauge{metric = "bytes_read"},
					barLineChart{
						metric "bytes_read"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "ep_mem_low_wat"
				widgets ([
					balanceGauge{metric = "ep_mem_low_wat"},
					barLineChart{
						metric "ep_mem_low_wat"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "ep_mem_high_wat"
				widgets ([
					balanceGauge{metric = "ep_mem_high_wat"},
					barLineChart{
						metric "ep_mem_high_wat"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "bytes_written"
				widgets([
					balanceGauge{metric = "bytes_written"},
					barLineChart {
						metric "bytes_written"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "mem_used"
				widgets ([
					balanceGauge{metric = "mem_used"},
					barLineChart{
						metric "mem_used"
						axisYUnit Unit.MEMORY
					}
				])
			}   
		])
	}	
	
			
}
