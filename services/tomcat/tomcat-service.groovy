/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
import java.util.concurrent.TimeUnit;
import static JmxMonitors.*

service {
	name "tomcat"
	icon "tomcat.gif"
	type "APP_SERVER"
	
    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2
	
	def instanceId = context.instanceId
	
	def portIncrement = context.isLocalCloud() ? instanceId-1 : 0
	def currJmxPort = jmxPort + portIncrement
	def currHttpPort = port + portIncrement
	def currAjpPort = ajpPort + portIncrement
	
	compute {
		template "SMALL_LINUX"
	}

	lifecycle {
		
		details {
			def currPublicIP = context.publicAddress
			def contextPath = context.attributes.thisInstance["contextPath"]
			if (contextPath == 'ROOT') contextPath="" // ROOT means "" by convention in Tomcat
			def applicationURL = "http://${currPublicIP}:${currHttpPort}/${contextPath}"
			println "tomcat-service.groovy: applicationURL is ${applicationURL}"
			
			return [
				"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
			]
		}

		monitors {
			def contextPath = context.attributes.thisInstance["contextPath"]
			if (contextPath == 'ROOT') contextPath="" // ROOT means "" by convention in Tomcat
			def metricNamesToMBeansNames = [
				"Current Http Threads Busy": ["Catalina:type=ThreadPool,name=\"http-bio-${currHttpPort}\"", "currentThreadsBusy"],
				"Current Http Thread Count": ["Catalina:type=ThreadPool,name=\"http-bio-${currHttpPort}\"", "currentThreadCount"],
				"Backlog": ["Catalina:type=ProtocolHandler,port=${currHttpPort}", "backlog"],
				"Total Requests Count": ["Catalina:type=GlobalRequestProcessor,name=\"http-bio-${currHttpPort}\"", "requestCount"],
				"Active Sessions": ["Catalina:type=Manager,context=/${contextPath},host=localhost", "activeSessions"],
			]
			return getJmxMetrics("127.0.0.1",currJmxPort,metricNamesToMBeansNames)
		}
		
		init    "tomcat_init.groovy"
		install "tomcat_install.groovy"
		start   "tomcat_start.groovy"
		preStop "tomcat_stop.groovy"
		
		startDetectionTimeoutSecs 240
		startDetection {
			println "tomcat-service.groovy(startDetection): arePortsFree http=${currHttpPort} ajp=${currAjpPort} ..."
			!ServiceUtils.arePortsFree([currHttpPort, currAjpPort] )
		}
		
		
		postStart {
			if ( useLoadBalancer ) { 
				println "tomcat-service.groovy: tomcat Post-start ..."
				def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
				println "tomcat-service.groovy: invoking add-node of apacheLB ..."
				
				def ipAddress = context.privateAddress
				if (ipAddress == null || ipAddress.trim() == "") ipAddress = context.publicAddress
				
				println "tomcat-service.groovy: ipAddress is ${ipAddress} ..."
				
				def contextPath = context.attributes.thisInstance["contextPath"]
				if (contextPath == 'ROOT') contextPath="" // ROOT means "" by convention in Tomcat
				def currURL="http://${ipAddress}:${currHttpPort}/${contextPath}"
				println "tomcat-service.groovy: About to add ${currURL} to apacheLB ..."
				apacheService.invoke("addNode", currURL as String, instanceId as String)
				println "tomcat-service.groovy: tomcat Post-start ended"
			}
		}
		
		postStop {
			if ( useLoadBalancer ) { 
				println "tomcat-service.groovy: tomcat Post-stop ..."
				try { 
					def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
					
					if ( apacheService != null ) { 
						def ipAddress = context.privateAddress
						if (ipAddress == null || ipAddress.trim() == "") ipAddress = context.publicAddress
						println "tomcat-service.groovy: ipAddress is ${ipAddress} ..."
						def contextPath = context.attributes.thisInstance["contextPath"]
						if (contextPath == 'ROOT') contextPath="" // ROOT means "" by convention in Tomcat
						def currURL="http://${ipAddress}:${currHttpPort}/${contextPath}"
						println "tomcat-service.groovy: About to remove ${currURL} from apacheLB ..."
						apacheService.invoke("removeNode", currURL as String, instanceId as String)
					}
					else {
						println "tomcat-service.groovy: waitForService apacheLB returned null"
					}
				}
				catch (all) {
					println "tomcat-service.groovy: Exception in Post-stop: " + all
				}
				println "tomcat-service.groovy: tomcat Post-stop ended"
			}
		}
	}

	customCommands ([
		"updateWar" : {warUrl -> 
			println "tomcat-service.groovy(updateWar custom command): warUrl is ${warUrl}..."
			if (! warUrl) return "warUrl is null. So we do nothing."
			context.attributes.thisService["warUrl"] = "${warUrl}"
			
			println "tomcat-service.groovy(updateWar customCommand): invoking updateWarFile custom command ..."
			def service = context.waitForService(context.serviceName, 60, TimeUnit.SECONDS)
			def currentInstance = service.getInstances().find{ it.instanceId == context.instanceId }
			currentInstance.invoke("updateWarFile")
			
			println "tomcat-service.groovy(updateWar customCommand): End"
			return true
		} ,
		 
		"updateWarFile" : "updateWarFile.groovy"
	])
	
	userInterface {
		
		metricGroups = ([
			metricGroup {
				name "process"
				metrics([
				    "Total Process Cpu Time",
					"Process Cpu Usage",
					"Total Process Virtual Memory",
					"Num Of Active Threads"
				])
			} ,
			metricGroup {
				name "http"
				metrics([
					"Current Http Threads Busy",
					"Current Http Thread Count",
					"Backlog",
					"Total Requests Count"
				])
			} ,

		])

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
			} ,
			widgetGroup {
				name "Total Process Virtual Memory"
				widgets([
					balanceGauge{metric = "Total Process Virtual Memory"},
					barLineChart {
						metric "Total Process Virtual Memory"
						axisYUnit Unit.MEMORY
					}
				])
			} ,
			widgetGroup {
				name "Num Of Active Threads"
				widgets ([
					balanceGauge{metric = "Num Of Active Threads"},
					barLineChart{
						metric "Num Of Active Threads"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
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
				name "Current Http Thread Count"
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
			} ,
			widgetGroup {
				name "Active Sessions"
				widgets([
					balanceGauge{metric = "Active Sessions"},
					barLineChart {
						metric "Active Sessions"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {
				name "Total Requests Count"
				widgets([
					balanceGauge{metric = "Total Requests Count"},
					barLineChart {
						metric "Total Requests Count"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {
				name "Total Process Cpu Time"
				widgets([
					balanceGauge{metric = "Total Process Cpu Time"},
					barLineChart {
						metric "Total Process Cpu Time"
						axisYUnit Unit.REGULAR
					}
				])
			}
		])
	}
	
	network {
		port = currHttpPort
		protocolDescription = "HTTP"
	}
	
	scaleCooldownInSeconds 300
	samplingPeriodInSeconds 1

	// Defines an automatic scaling rule based on "counter" metric value
	scalingRules ([
		scalingRule {

			serviceStatistics {
				metric "Current Http Threads Busy"
				statistics Statistics.maximumOfMaximums
				movingTimeRangeInSeconds 20
			}

			highThreshold {
				value 10
				instancesIncrease 1
			}

			lowThreshold {
				value 1
				instancesDecrease 1
			}
		}
	])
}