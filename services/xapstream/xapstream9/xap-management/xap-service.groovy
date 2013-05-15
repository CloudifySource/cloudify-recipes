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

import util

/**

Manages XAP management node(s).  Also starts Web UI.

**/
service {

	name "xap-management"
	type "APP_SERVER"
	icon "xap.png"
	elastic false
	numInstances (context.isLocalCloud()?1:2 )
	minAllowedInstances 1
	maxAllowedInstances 2

	compute {
       	 	template "MEDIUM_LINUX"
    	}

	def instanceId=context.instanceId

	lifecycle{


		install "xap_install.groovy"

		start "xap_start.groovy"

	        startDetectionTimeoutSecs 60
        	startDetection {
            		ServiceUtils.isPortOccupied(uiPort)
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

	customCommands ([

		//Public entry points

		"deploy-pu"	: {puurl->
					util.invokeLocal(context,"_deploy-pu", [
						"deploy-pu-puurl":puurl
						"deploy-pu-schema":schema,
						"deploy-pu-partitions":partitions,
						"deploy-pu-backups":backups,
						"deploy-pu-maxpervm":maxpervm,
						"deploy-pu-maxpermachine":maxpermachine
					])
				  }
		"deploy-grid"	: {schema,partitions,backups,maxpervm,maxpermachine->
					util.invokeLocal(context,"_deploy-grid", [
						"deploy-grid-schema":schema,
						"deploy-grid-partitions":partitions,
						"deploy-grid-backups":backups,
						"deploy-grid-maxpervm":maxpervm,
						"deploy-grid-maxpermachine":maxpermachine
					])
				   }

		// NOT SURE HOW THIS SHOULD WORK
		//"deploy-gateway": {util.invokeLocal(context,"_deploy-gateway",

		//Actual parameterized calls
		"_deploy-pu	: "commands/deploy-pu.groovy",
		"_deploy-grid"	: "commands/deploy-grid.groovy",
		//"_deploy-gateway": "commands/deploy-gateway.groovy"

	])


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


