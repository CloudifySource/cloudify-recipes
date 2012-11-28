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
	icon "feather-small.gif"
	type "WEB_SERVER"
	elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2

	compute {
		template "SMALL_LINUX"
	}
	
	def instanceID = context.instanceId
	def portIncrement =  context.isLocalCloud() ? instanceID-1 : 0			
	def currentPort = port + portIncrement
	
	
	
	lifecycle {
	
	
		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP =InetAddress.getLocalHost().getHostAddress()
			}
			else {
				currPublicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
				
			def hostAndPort="http://${currPublicIP}:${currentPort}"
			def applicationURL = "${hostAndPort}/"+ctxPath
		
			def detailsMap = ["Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"]
			
			if ( php=="true" ) {
				phpInfo="${hostAndPort}/index.php"
				detailsMap.put("phpinfo","<a href=\"${phpInfo}\" target=\"_blank\">${phpInfo}</a>")
			}
								
			return detailsMap
		}	
				
		install "apache_install.groovy"
					
		postInstall "apache_postInstall.groovy"
		
		start "apache_start.groovy"		
			
		startDetectionTimeoutSecs 800
		startDetection {			
			ServiceUtils.isPortOccupied(currentPort)
		}	
		
		postStart "apache_postStart.groovy"
		
		preStop "apache_stop.groovy"
		
		postStop "apache_postStop.groovy"
			
		shutdown "apache_uninstall.groovy"	
			
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.re=httpd|apache")
			println "apache-service.groovy: current PIDs: ${myPids}"
			return myPids
        }				
	}
	
	userInterface load("apache_userInterface.groovy")	
	
	network {
		port currentPort
		protocolDescription "HTTP"
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
				value 40
				instancesIncrease 1
			}

			lowThreshold {
				value 25
				instancesDecrease 1
			}
		}	
	])	
		
}
