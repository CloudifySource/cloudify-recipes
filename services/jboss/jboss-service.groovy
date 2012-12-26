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

/* 
   This recipe enables users to install JBoss 7.1.0 on any cloud (you can configure it to deploy other JBoss versions by modifying the jboss-service.properties file).
	
*/


import static JmxMonitors.*
import java.util.concurrent.TimeUnit;

service {
   
    name "jboss"
	icon "jboss_logo.png"
	type "APP_SERVER"
	
	elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2
	
	def instanceID = context.instanceId
	def portIncrement =  context.isLocalCloud() ? instanceID-1 : 0				
	def currHttpPort = jbossPort + portIncrement
	def currJmxPort = jmxPort + portIncrement

	lifecycle{
	
	
		/* 
			  The "Application URL" which is calculated here, will be displayed in Cloudify's Web UI in the JBoss service instance details panel.
			  This will enable users to access the application directly at : 
			    THIS_SERVICEINSTANCE_IP_ADDRESS:port/applicationName 
		*/
		
		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP = InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP =context.getPublicAddress()
			}
	
			def ctxPath=("default" == context.applicationName)?"":"${context.applicationName}"			
			def applicationURL = "http://${currPublicIP}:${currHttpPort}/${ctxPath}"
		
				return [
					"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
				]
		}	
		
		def metricNamesToMBeansNames = [				
				"Total Requests Count": ["jboss.as:subsystem=web,connector=http", "requestCount"]				
		]	
		
		
		def currTimeStamp
		def prevRequests = 0
		def prevTimeStamp = 0
		def reqsPerSec = 0 		
		
		monitors {
									
			def jmxUrl = "service:jmx:remoting-jmx://127.0.0.1:${currJmxPort}"
			def currMetrics = getJmxMetrics(jmxUrl,metricNamesToMBeansNames)
			def totalRequests = currMetrics["Total Requests Count"] as long
			
			/* 
			   The "Total Requests Count" metric is retrieved from the JMX 
			   and since we want to use it in the Scaling Rules Mechanism 
			   and display the requests per second in the Web UI, we calculate it here, in the monitors section.
               The calculation of "Requests Per Second" is based on the time 
			   and the "Total Requests Count" metric value from the JMX.
			*/
				
			if ( prevTimeStamp == 0 ) {
				prevTimeStamp =System.currentTimeMillis();			
			}
						
			currTimeStamp =System.currentTimeMillis()			
			def currDiffInSecs = (currTimeStamp-prevTimeStamp)/1000
			def currDelta = 0 
									
			if ( totalRequests == 0 ) {
				reqsPerSec = 0
			}
			else { 				
				/* Check only every 20 seconds */ 
				if ( currDiffInSecs >  19 ) {
					currDelta=totalRequests-prevRequests					
					reqsPerSec = Math.floor(currDelta/currDiffInSecs) as int					
					prevTimeStamp = currTimeStamp
					prevRequests = totalRequests
				}
				/* else use the previous value of reqsPerSec */ 
			}
			
			currMetrics.put("Requests Per Second",reqsPerSec)			
									
			return currMetrics
    	}		
	
	
		init "jboss_install.groovy"		
		postInstall "jboss_postInstall.groovy"	
		start "jboss_start.groovy"
		preStop "jboss_stop.groovy"
		
		startDetectionTimeoutSecs 200
		startDetection {
			println "startDetection: Testing port ${currHttpPort} ..."
			ServiceUtils.isPortOccupied(currHttpPort)							
		}
		
		
		
		postStart {			
			if ( useLoadBalancer ) { 
				println "jboss-service.groovy: jboss Post-start ..."
				def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)			
				println "jboss-service.groovy: invoking add-node of apacheLB ..."
					
				def ctxPath = ("default" == context.applicationName)?"":"${context.applicationName}"				
				
				def privateIP
				if (  context.isLocalCloud()  ) {
					privateIP=InetAddress.getLocalHost().getHostAddress()
				}
				else {
					privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
				}
				println "jboss-service.groovy: privateIP is ${privateIP} ..."
				
				def currURL="http://${privateIP}:${currHttpPort}/${ctxPath}"
				println "jboss-service.groovy: About to add ${currURL} to apacheLB ..."
				apacheService.invoke("addNode", currURL as String, instanceID as String)			                 
				println "jboss-service.groovy: jboss Post-start ended"
			}			
		}
		
		postStop {
			if ( useLoadBalancer ) { 
				println "jboss-service.groovy: jboss Post-stop ..."
				try { 
					def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)			
							
					if ( apacheService != null ) { 						
						def ctxPath = ("default" == context.applicationName)?"":"${context.applicationName}"
						println "jboss-service.groovy: postStop ctxPath is ${ctxPath}"				
						
						def privateIP
						if (  context.isLocalCloud()  ) {
							privateIP=InetAddress.localHost.hostAddress
						}
						else {
							privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
						}				
						
						println "jboss-service.groovy: privateIP is ${privateIP} ..."
						def currURL="http://${privateIP}:${currHttpPort}/${ctxPath}"
						println "jboss-service.groovy: About to remove ${currURL} from apacheLB ..."
						apacheService.invoke("removeNode", currURL as String, instanceID as String)
					}
					else {
						println "jboss-service.groovy: waitForService returned apacheLB null"
					}
				}
				catch (all) {		
					println "jboss-service.groovy: Exception in Post-stop: " + all
				} 				
				println "jboss-service.groovy: jboss Post-stop ended"
			}			
		}					
	}
	
	userInterface {

		metricGroups = ([	
			metricGroup {

				name "http"

				metrics([					
					"Total Requests Count",
					"Requests Per Second"
				])
			} ,

		])

		widgetGroups = ([					
			widgetGroup {
				name "Total Requests Count"
				widgets([
					balanceGauge{metric = "Total Requests Count"},
					barLineChart {
						metric "Total Requests Count"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Requests Per Second"
				widgets([
					balanceGauge{metric = "Requests Per Second"},
					barLineChart {
						metric "Requests Per Second"
						axisYUnit Unit.REGULAR
					}
				])
			}				
		])
	}
	
	network {
        port = currHttpPort
        protocolDescription ="HTTP"
    }
	
	scaleInCooldownInSeconds 60
    scaleOutCooldownInSeconds 300	
	samplingPeriodInSeconds 5

	/* 
	   Scaling rules : 
		If your deployed JBoss instance receives more than fifty requests per second for a period of twenty seconds, 
		 then another Jboss service instance will be installed on the cloud and be added to a load balancer.
		 
	*/ 
	scalingRules ([
		scalingRule {

			serviceStatistics {
				metric "Requests Per Second"
				statistics Statistics.averageOfAverages
				movingTimeRangeInSeconds 20
			}

			highThreshold {
				value 50
				instancesIncrease 1
			}

			lowThreshold {
				value 15
				instancesDecrease 1
			}
		}
	])
}	
 

	
