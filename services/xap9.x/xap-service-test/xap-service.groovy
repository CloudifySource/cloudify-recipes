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

	name "xap9.x"
	type "APP_SERVER"
	icon "xap.png"
	elastic true
	// 3 instances, because 2 instances are reserved for
	// management.  Additional instances are standalone
	// GSCs.  For local cloud, first instance is
	// gsm, gsc, webui; after that gscs
	numInstances context.isLocalCloud()?1:3 
	minAllowedInstances context.isLocalCloud()?1:3
	maxAllowedInstances context.isLocalCloud()?1:200


    compute {
	// This template is applied to all instances.  GSCs
	// are be sized to match the available memory.
        template "MEDIUM_LINUX"
    }

	def instanceId=context.instanceId
	def managementNode=(instanceId<3)

	lifecycle{


		install "xap_install.groovy"

		start "xap_start.groovy"

	        startDetectionTimeoutSecs 60
        	startDetection {
			if (context.isLocalCloud()){
            			if(context.instanceId==1){ServiceUtils.isPortOccupied(uiPort)
				}
				//TODO -- Just a GSC started... how to detect?
				else{
					true
				}
			}
			else{
            			if(context.instanceId<3){ServiceUtils.isPortOccupied(uiPort)
				}
				//TODO -- Just a GSC started... how to detect?
				else{
					true
				}
			}
        	}
		locator {
			uuid=context.attributes.thisInstance.uuid
			i=0
			while (uuid==null){
				uuid=context.attributes.thisInstance.uuid
				Thread.sleep 1000
				if (i>10){
					println "TIMEDOUT"
					break
				}
				i=i+1
			}
			def pids=ServiceUtils.ProcessUtils.getPidsWithQuery("Args.*.ct=${uuid}");
			println "LOCATOR CALLED FOR UUID ${uuid}: pids=${pids}"
			return pids
		}

		preStop {
			println "STOPPING INSTANCE ${context.instanceId}"
		}


		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP = InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP =context.getPublicAddress()
			}
	
			def applicationURL = "http://${currPublicIP}:${uiPort}"
		
				return [
					"Management UI":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
				]
		}
	}


	userInterface {
		metricGroups = ([
			metricGroup {

				name "server"

				metrics([
				"Cluster Uptime Secs",
				"Topology Count",
				"Executor Count",
				"Task Count",
				"Worker Count"
				])
			},
		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Cluster Uptime Secs"
				widgets ([
					barLineChart{
						metric "Cluster Uptime Secs"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Topology Count"
				widgets ([
					barLineChart{
						metric "Topology Count"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Executor Count"
				widgets ([
					barLineChart{
						metric "Executor Count"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Task Count"
				widgets ([
					barLineChart{
						metric "Task Count"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Worker Count"
				widgets ([
					barLineChart{
						metric "Worker Count"
						axisYUnit Unit.REGULAR
					}
				])
			},
		]
		)
	}
}


