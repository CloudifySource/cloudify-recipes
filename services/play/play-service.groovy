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
	name "play"
	icon "playIcon.png"
	type "WEB_SERVER"
		
    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2
	
	compute {
		template "SMALL_LINUX"
	}	
	
	lifecycle {
	
		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP = InetAddress.localHost.hostAddress				
			}
			else {
				currPublicIP =context.getPublicAddress()				
			}
			def baseURL	= "http://${currPublicIP}:${httpPort}"
							
			
			def applicationURL = "${baseURL}/${applicationCtxPath}"
		
				return [					
					"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
				]
		}		
	
	
		install "play_install.groovy"
		preStart "play_preStart.groovy" 
		start "play_start.groovy"
		startDetectionTimeoutSecs 400
		startDetection {
			!ServiceUtils.isPortFree(httpPort)
		}
		
		def instanceID = context.instanceId
		
		postStart {			
			if ( useLoadBalancer ) { 
				println "play-service.groovy: play Post-start ..."
				def apacheService = context.waitForService(loadBalancerServiceName, 180, TimeUnit.SECONDS)			
				println "play-service.groovy: invoking add-node of apacheLB ..."
					
							
				def privateIP
				if (  context.isLocalCloud()  ) {
					privateIP=InetAddress.getLocalHost().getHostAddress()
				}
				else {
					privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
				}
				println "play-service.groovy: privateIP is ${privateIP} ..."
				
				def currURL="http://${privateIP}:${httpPort}"
				println "play-service.groovy: About to add ${currURL} to apacheLB ..."
				apacheService.invoke("addNode", currURL as String, instanceID as String)			                 
				println "play-service.groovy: play Post-start ended"
			}			
		}
		
		postStop {
			if ( useLoadBalancer ) { 
				println "play-service.groovy: play Post-stop ..."
				try { 				
					def apacheService = context.waitForService(loadBalancerServiceName, 180, TimeUnit.SECONDS)			
					if ( apacheService != null ) { 	
											
						def privateIP
						if (  context.isLocalCloud()  ) {
							privateIP=InetAddress.localHost.hostAddress
						}
						else {
							privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
						}				
						
						println "play-service.groovy: privateIP is ${privateIP} ..."
						def currURL="http://${privateIP}:${httpPort}"
						println "play-service.groovy: About to remove ${currURL} from apacheLB ..."
						apacheService.invoke("removeNode", currURL as String, instanceID as String)
					}
					else {
						println "play-service.groovy: waitForService returned ${loadBalancerServiceName} null"
					}
				}
				catch (all) {		
					println "play-service.groovy: Exception in Post-stop: " + all
				} 					
				println "play-service.groovy: play Post-stop ended"
			}			
		}		
		
	}

	customCommands ([   
		/* 
			This custom command enables users to update their application
			Usage: 
				invoke play updateApp http://www.mynewapplication.zip
		*/
		"updateApp" : {appZipUrl -> 
			println "play-service.groovy(updateApp custom command): AppZipUrl is ${appZipUrl}..."
			installDir = System.properties["user.home"]+ "/.cloudify/${serviceName}" + context.instanceId
			applicationZip = "${installDir}/${applicationZipName}"

			def builder = new AntBuilder()
			println "Getting updated application ${appZipUrl}..."
			builder.get(src:"${appZipUrl}", dest:"${applicationZip}", skipexisting:false)
			builder.unzip(src:"${applicationZip}", dest:"${installDir}", overwrite:true)
			println "Copying ${applicationName} to " + context.serviceDirectory + "/${name}/playApps ..."
			builder.move(file:"${installDir}/${applicationName}", tofile:context.serviceDirectory +"/${name}/playApps/${applicationName}", overwrite:true)			
			return true
		},
		
		
		/* 
			This custom command enables users to replace a string in a file (relative to play home folder
				Usage : invoke play replace all|first origString newString relativePath
		
			Examples: 
					1. The following replaces all the occurrences of DEBUG with ERROR 
						in PLAY_HOME_FOLDER/framework/src/play/src/main/resources/reference.conf : 
						invoke play replace all "DEBUG" "ERROR" /framework/src/play/src/main/resources/reference.conf
			
					2. The following replaces the 1st occurrence of DEBUG with ERROR 
						in PLAY_HOME_FOLDER/framework/src/play/src/main/resources/reference.conf : 
						invoke play replace first "DEBUG" "ERROR" /framework/src/play/src/main/resources/reference.conf			
		*/	
		
		"replace" : "play_relpacer.groovy" , 
		
		/* The following invokes a play command line and up to 3 arguments.
		   Here are some examples : 
		     Usage invoke play cmd nameOfTheCommandLine arg1 arg2 ...
		    1. invoke play cmd compile
		    2. invoke play cmd clean
		    3. invoke play cmd clean-all
		    4. invoke play cmd test
		    5. invoke play cmd package
		    6. invoke play cmd update
		  */
		"cmd" : "play_cmd.groovy" 
	])
	
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
	
	
	network {
        port = httpPort
        protocolDescription ="HTTP"
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
