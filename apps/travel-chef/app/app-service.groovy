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
import static JmxMonitors.*
import java.util.concurrent.TimeUnit;

service {
    extend "../../../services/chef"
    name "app"
    type "APP_SERVER"
    icon "spring.png"
    
    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle {
        startDetectionTimeoutSecs 600
        startDetection {
			ServiceUtils.arePortsOccupied([8080, 11099])
		}

		stopDetection {
			!(ServiceUtils.isPortOccupied(8080))
		}
				
		details {
			def travelAppUrl = "http://"+System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]+":8080/travel"
    		return [
    			"Travel App URL":"<a href=\"${travelAppUrl}\" target=\"_blank\">${travelAppUrl}</a>"
    		]
    	}
    	
    	monitors {													
			def metricNamesToMBeansNames = [
				"Current Http Threads Busy": ["Catalina:type=ThreadPool,name=http-8080", "currentThreadsBusy"],
				"Current Http Thread Count": ["Catalina:type=ThreadPool,name=http-8080", "currentThreadCount"],
				"Backlog": ["Catalina:type=ProtocolHandler,port=8080", "backlog"],
				"Total Requests Count": ["Catalina:j2eeType=Servlet,name=travel,WebModule=//localhost/travel,J2EEApplication=none,J2EEServer=none", "requestCount"],
				"Active Sessions": ["Catalina:type=Manager,path=/travel,host=localhost", "activeSessions"],
			]
			
			return getJmxMetrics("127.0.0.1",11099,metricNamesToMBeansNames)
    	}
    	
    	def instanceID = context.instanceId
    	
    	postStart {			
			def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)						
			def privateIP = System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
			def currURL="http://${privateIP}:8080/${context.applicationName}"
			apacheService.invoke("addNode", currURL as String, instanceID as String)			                 
		}
		
		postStop {
			try { 	
				def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
				if ( apacheService != null ) { 					
					def	privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
					def currURL="http://${privateIP}:8080/${context.applicationName}"
					apacheService.invoke("removeNode", currURL as String, instanceID as String)
				}
			}
			catch (all) {		
				println "app-service.groovy: Exception in Post-stop: " + all
			} 
		}
    	
    }

	
	userInterface {

		metricGroups = ([
			metricGroup {
				name "process"
				metrics([
					"Process Cpu Usage",
					"Total Process Virtual Memory",
					"Num Of Active Threads"
				])
			} ,
			metricGroup {
				name "http"
				metrics([
					"Current Http Threads Busy",
					"Current Http Threads Count",
					"Backlog",
					"Total Requests Count"
				])
			} ,
		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Process Cpu Usage"
				widgets ([
					balanceGauge{metric = "Process Cpu Usage"},
					barLineChart{
						metric "Process Cpu Usage"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name "Total Process Virtual Memory"
				widgets([
					balanceGauge{metric = "Total Process Virtual Memory"},
					barLineChart {
						metric "Total Process Virtual Memory"
						axisYUnit Unit.MEMORY
					}
				])
			},
			widgetGroup {
				name "Num Of Active Threads"
				widgets ([
					balanceGauge{metric = "Num Of Active Threads"},
					barLineChart{
						metric "Num Of Active Threads"
						axisYUnit Unit.REGULAR
					}
				])
			}     ,
			widgetGroup {

				name "Current Http Threads Busy"
				widgets([
					balanceGauge{metric = "Current Http Threads Busy"},
					barLineChart {
						metric "Current Http Threads Busy"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {

				name "Current Http Threads Count"
				widgets([
					balanceGauge{metric = "Current Http Thread Count"},
					barLineChart {
						metric "Current Http Thread Count"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {

				name "Request Backlog"
				widgets([
					balanceGauge{metric = "Backlog"},
					barLineChart {
						metric "Backlog"
						axisYUnit Unit.REGULAR
					}
				])
			}  ,
			widgetGroup {
				name "Active Sessions"
				widgets([
					balanceGauge{metric = "Active Sessions"},
					barLineChart {
						metric "Active Sessions"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Total Requests Count"
				widgets([
					balanceGauge{metric = "Total Requests Count"},
					barLineChart {
						metric "Total Requests Count"
						axisYUnit Unit.REGULAR
					}
				])
			}
		]
		)
	}
	
	
	scaleCooldownInSeconds 180
	samplingPeriodInSeconds 1

	// Defines an automatic scaling rule based on "Active Sessions" metric value
	scalingRules ([
		scalingRule {

			serviceStatistics {
				metric "Active Sessions" 
				statistics Statistics.averageOfAverages
				movingTimeRangeInSeconds 20
			}

			highThreshold {
				value 2
				instancesIncrease 1
			}

			/*
			lowThreshold {
				value 0
				instancesDecrease 1
			}
			*/
		}
	])
	
}
