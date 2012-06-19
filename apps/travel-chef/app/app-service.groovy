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
import static JmxMonitors.*

service {
    extend "../../../services/chef"
    name "app"
    type "APP_SERVER"
    icon "spring.png"
    
    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2
    
    lifecycle {
    	start "app_install.groovy"

		startDetection {
			ServiceUtils.isPortOccupied(8080)
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
			def server = connectRMI("127.0.0.1", 11099)			
			return [
				"Current Http Threads Busy":getJMXAttribute(server, "Catalina:type=ThreadPool,name=http-8080", "currentThreadsBusy"),
				"Current Http Thread Count":getJMXAttribute(server, "Catalina:type=ThreadPool,name=http-8080", "currentThreadCount"), 
			    "Backlog":getJMXAttribute(server, "Catalina:type=ProtocolHandler,port=8080", "backlog"), 
				"Active Sessions":getJMXAttribute(server, "Catalina:type=Manager,path=/travel,host=localhost", "activeSessions"), 
			    "Total Requests Count":getJMXAttribute(server, "Catalina:j2eeType=Servlet,name=travel,WebModule=//localhost/travel,J2EEApplication=none,J2EEServer=none", "requestCount")
			]
    	}
    }
    compute {
        template "MEDIUM_LINUX"
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
	
	
	scaleCooldownInSeconds 120
	samplingPeriodInSeconds 1

	// Defines an automatic scaling rule based on "counter" metric value
	scalingRules ([
		scalingRule {

			serviceStatistics {
				metric "Total Requests Count"
				statistics Statistics.maximumThroughput
				movingTimeRangeInSeconds 10
			}

			highThreshold {
				value 1
				instancesIncrease 1
			}

			lowThreshold {
				value 0.2
				instancesDecrease 1
			}
		}
	])
	
}
