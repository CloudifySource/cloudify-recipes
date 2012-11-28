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
import java.util.concurrent.TimeUnit
service {
	name "vertx"
	icon "vertx.png"
	type "WEB_SERVER"


    def appUrl
    if (  context.isLocalCloud()  ) {
        appUrl = "http://${InetAddress.localHost.hostAddress}:${applicationPort}${applicationRoot}"
    }
    else {
		def currPublicIP =context.getPublicAddress()
        appUrl = "http://${currPublicIP}:${applicationPort}${applicationRoot}"
    }

    url appUrl
    elastic true
	numInstances 1
	minAllowedInstances 1

	lifecycle {
	
		details {
            [ "Application Name": applicationName, "Run Mode":runMode]
		}		
	
	
		install "vertx_install.groovy"
		start "vertx_start.groovy"
		startDetectionTimeoutSecs 240
		startDetection {
			!ServiceUtils.isPortFree(applicationPort)
		}
		
		def instanceID = context.instanceId
		
		postStart {			
			if ( useLoadBalancer ) { 
				println "vertx-service.groovy: vertx Post-start ..."
				def apacheService = context.waitForService(loadBalancerServiceName, 180, TimeUnit.SECONDS)
				println "vertx-service.groovy: invoking add-node of load balancer service ${loadBalancerServiceName}..."
					
							
				def privateIP
				if (  context.isLocalCloud()  ) {
					privateIP=InetAddress.getLocalHost().getHostAddress()
				}
				else {
					privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
				}
				println "vertx-service.groovy: privateIP is ${privateIP} ..."
				
				def currURL="http://${privateIP}:${applicationPort}/${applicationRoot}"
				println "vertx-service.groovy: About to add ${currURL} to apacheLB ..."
				apacheService.invoke("addNode", currURL as String, instanceID as String)			                 
				println "vertx-service.groovy: vertx Post-start ended"
			}			
		}
		
		postStop {
			if ( useLoadBalancer ) { 
				println "vertx-service.groovy: vertx Post-stop ..."
				def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)			
										
										
				def privateIP
				if (  context.isLocalCloud()  ) {
					privateIP=InetAddress.localHost.hostAddress
				}
				else {
					privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
				}				
				
				println "vertx-service.groovy: privateIP is ${privateIP} ..."
				def currURL="http://${privateIP}:${applicationPort}/${applicationRoot}"
				println "vertx-service.groovy: About to remove ${currURL} from apacheLB ..."
				apacheService.invoke("removeNode", currURL as String, instanceID as String)
				println "vertx-service.groovy: play Post-stop ended"
			}			
		}		
		
	}

	userInterface {

		metricGroups = ([
			metricGroup {

				name "process"

				metrics([
				    "Total Process Cpu Time"					
				])
			} 
		])

		widgetGroups = ([								
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
	
	
	scaleCooldownInSeconds 20
	samplingPeriodInSeconds 1

	
	scalingRules ([
		scalingRule {

			serviceStatistics {
				metric "Total Process Cpu Time"
				timeStatistics Statistics.averageCpuPercentage
			    instancesStatistics Statistics.maximum
				movingTimeRangeInSeconds 20
			}

			highThreshold {
				value 60
				instancesIncrease 1
			}

			lowThreshold {
				value 25
				instancesDecrease 1
			}
		}
	])

}
