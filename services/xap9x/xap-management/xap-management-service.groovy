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

import org.openspaces.admin.AdminFactory
import org.openspaces.admin.Admin
import java.util.concurrent.TimeUnit
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

	def admin = null

	compute {
       	 	template "${template}"
    	}

	lifecycle{


		install "xap_install.groovy"

		start "xap_start.groovy"

	        startDetectionTimeoutSecs 60
        	startDetection {
            		ServiceUtils.isPortOccupied(uiPort)
        	}

		locator {
			uuid=context.attributes.thisInstance.uuid
			i=0
			while (uuid==null){
				Thread.sleep 1000
				uuid=context.attributes.thisInstance.uuid
				if (i>10){
					println "LOCATOR TIMED OUT"
					break
				}
				i++
			}
			if(i>11)return null

			i=0
			def pids=[]
			while(pids.size()==0){
				pids=ServiceUtils.ProcessUtils.getPidsWithQuery("Args.*.ct=${uuid}");
				i++
				if(i>10){
					println "PROCESS NOT DETECTED"
					break
				}
				Thread.sleep(1000)
			}

			return pids
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
		
		monitors {
			if(admin==null){
				admin = new AdminFactory()
				.useDaemonThreads(true)
				.addLocators("${InetAddress.getLocalHost().getHostAddress()}:"+lusPort)
				.create();
			}

			def gscs=admin.gridServiceContainers
			gscs.waitFor(200,1,TimeUnit.MILLISECONDS)
			def pus=admin.processingUnits

			//memory & cpu
			def vmstats=admin.virtualMachines.statistics

			return [
				"Container Count":gscs.size,
				"PU Count":pus.size,
				"Heap Used %":vmstats.memoryHeapUsedPerc,
				"Heap Used GB":vmstats.memoryHeapUsedInGB,
				"GC Collection Time":vmstats.gcCollectionTime
				]
		}
	}

	customCommands ([
//Public entry points

		"deploy-pu": {puname, puurl,schema,partitions,backups,maxpervm,maxpermachine ->
			util.invokeLocal(context,"_deploy-pu", [
				"deploy-pu-puurl":puurl,
				"deploy-pu-schema":schema,
				"deploy-pu-partitions":partitions,
				"deploy-pu-backups":backups,
				"deploy-pu-maxpervm":maxpervm,
				"deploy-pu-maxpermachine":maxpermachine,
				"deploy-pu-puname":puname
			])
		 },
		"deploy-pu-basic": {puurl->
			util.invokeLocal(context,"_deploy-pu", [
				"deploy-pu-puurl":puurl,
				"deploy-pu-schema":"none",
				"deploy-pu-partitions":1,
				"deploy-pu-backups":0,
				"deploy-pu-maxpervm":1,
				"deploy-pu-maxpermachine":1,
				"deploy-pu-puname":(new File(puurl).name)
			])
		},
					
		"deploy-grid"	: {name,schema,partitions,backups,maxpervm,maxpermachine->
			util.invokeLocal(context,"_deploy-grid", [
				"deploy-grid-name":name,
				"deploy-grid-schema":schema,
				"deploy-grid-partitions":partitions,
				"deploy-grid-backups":backups,
				"deploy-grid-maxpervm":maxpervm,
				"deploy-grid-maxpermachine":maxpermachine
			])
		},
		"undeploy-grid" : { name ->
			util.invokeLocal(context,"_undeploy-grid", [
				"undeploy-grid-name":name
			])
		},


		//Actual parameterized calls
		"_deploy-pu"	: "commands/deploy-pu.groovy",
		"_deploy-grid"	: "commands/deploy-grid.groovy",
		"_undeploy-grid": "commands/undeploy-grid.groovy",

	])


	userInterface {
		metricGroups = ([
			metricGroup {

				name "Containers"

				metrics([
					"Container Count",
				])
			},
			metricGroup {

				name "Processing Units"

				metrics([
					"PU Count",
				])
			},
			metricGroup {

				name "Java VM Stats"

				metrics([
					"Heap Used %",
					"Heap Used GB",
					"GC Collection Time"
				])
			}
		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Container Count"
				widgets ([
					barLineChart{
						metric "Container Count"
						axisYUnit Unit.REGULAR
					},
				])
			},
			widgetGroup {
				name "PU Count"
				widgets ([
					barLineChart{
						metric "PU Count"
						axisYUnit Unit.REGULAR
					},
				])

			},
			widgetGroup {
				name "VM Heap Used %"
				widgets ([
					balanceGauge{metric = "Heap Used %"},
					barLineChart{
						metric "Heap Used %"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name "VM Heap Used GB"
				widgets ([
					balanceGauge{metric = "Heap Used GB"},
					barLineChart{
						metric "Heap Used GB"
						axisYUnit Unit.MEMORY
					},
				])
			},
			widgetGroup {
				name "VM GC Collection Time"
				widgets ([
					balanceGauge{metric = "GC Collection Time"},
					barLineChart{
						metric "GC Collection Time"
						axisYUnit Unit.DURATION
					},
				])
			}
		])
	}
}


