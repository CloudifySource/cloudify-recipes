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
import java.util.concurrent.TimeUnit;
import util


service {

	name "storm-nimbus"
	type "APP_SERVER"
	icon "storm.png"
	elastic false
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 1

    compute {
        template "MEDIUM_LINUX"
    }

	lifecycle{


		install "storm_install.groovy"
		start "storm_start.groovy"
		preStop "storm_stop.groovy"

		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP = InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP =context.getPublicAddress()
			}
	
			def applicationURL = "http://${currPublicIP}:8080"
		
				return [
					"Storm UI":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
				]
		}

	}
	plugins([
		plugin {
			name "portLiveness"
			className "org.cloudifysource.usm.liveness.PortLivenessDetector"
			config ([
						"Port" : [6627],
						"TimeoutInSeconds" : 60,
						"Host" : "127.0.0.1"
					])
		}, 
		plugin {
			name "storm-nimbus"
			className "org.cloudifysource.storm.plugins.StormNimbusPlugin"
			config([
				"Cluster Uptime Secs":"uptime_secs",
				"Topology Count":"topology_count",
				"Executor Count":"executor_count",
				"Task Count":"task_count",
				"Worker Count":"worker_count"
			])
		} 

	])

	customCommands ([

		"addhostentry": { ip,hostname->
			if(! util.lockFile("/etc/hosts"))return false;
			try{
			"chmod ugo+rwx ${context.serviceDirectory}/commands/addhost.sh".execute()
			println "running addhost.sh with args ${ip} ${hostname}";
			"${context.serviceDirectory}/commands/addhost.sh ${ip} ${hostname}".execute()
			}
			finally{ util.unlockFile("/etc/hosts");}
			return true
		},

		"wordcount-start": "commands/wordcount-start.sh",

		/*
			Deploys a topology that utilizes XAP for streaming and/or
 			persistence.  Assumes a xapstream instance is part of the application */ 

		"deploy-xapstream" : {topoUrl, className, topoName, Object[] args -> 
			context.attributes.thisService["topoUrl"] = "${topoUrl}"
			context.attributes.thisService["topoName"] = "${topoName}"
			context.attributes.thisService["className"] = "${className}"
			argline=""
			for(arg in args){
				argline+=arg.toString()+" "
			}
				
			context.attributes.thisService["args"] = "${argline}"

			println "storm-service.groovy(deploy custom command): topoUrl is ${topoUrl}..."
			println "storm-service.groovy(deploy customCommand): invoking deploy custom command ..."
			nimbusService = context.waitForService("storm-nimbus", 60, TimeUnit.SECONDS)
			assert nimbusService!=null
			nimbusInstances = nimbusService.waitForInstances(1,60, TimeUnit.SECONDS)				
			xapService = context.waitForService("${xapManagerName}", 60, TimeUnit.SECONDS)
			xapInstances = xapService.waitForInstances(1,60,TimeUnit.SECONDS)
			context.attributes.thisService["xapHost"]="${xapInstances[0].getHostAddress()}:${lusPort}"

			instanceID = context.getInstanceId()			                       
			if ( instanceID == nimbusInstances[0].instanceId ) {
				println "storm-service.groovy(deploy customCommand):  instanceID is ${instanceID} now invoking deploy-xapstream-file..."
				nimbusInstances[0].invoke("deploy-xapstream-file")
			}
						
			println "storm-service.groovy(deploy-xapstream customCommand): End"
			return true
		} ,
		 

		"deploy-xapstream-file":"commands/deploy-xapstream.groovy",


		"kill":{ name ->
			"${context.serviceDirectory}/${script} kill ${name}".execute()
			return true
		}
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


