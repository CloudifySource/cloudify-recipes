/*******************************************************************************
* Copyright (c) 2014 GigaSpaces Technologies Ltd. All rights reserved
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

        postStop "xap_postStop.groovy"

	        startDetectionTimeoutSecs 180
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
			def currPublicIP = context.getPublicAddress()

			if (  context.isLocalCloud()  ) {
				currPort = 9099
			}
			else {
                currPort = 8099
			}

			def applicationURL = "http://${currPublicIP}:${currPort}"
            def xapInstallationDir = "${context.serviceDirectory}/${installDir}/${name}/"
            def interactiveShellURL = "http://${currPublicIP}:8081/wd/${xapInstallationDir}/bin"
            def xapShellURL = "http://${currPublicIP}:8081/wd/${xapInstallationDir}/tools/groovy/bin"
            if (butterflyEnabled) {
                return [
                        "Management UI":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>",
                        "GigaSpaces Interactive Shell URL":"<a href=\"${interactiveShellURL}\" target=\"_blank\">${interactiveShellURL}</a>",
                        "Groovy Interactive Shell URL":"<a href=\"${xapShellURL}\" target=\"_blank\">${xapShellURL}</a>"
                ]
            } else {
                return [
                        "Management UI":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
                ]
            }
		}
		
		monitors {
			if(admin==null){

                ip=context.getPrivateAddress()
				admin = new AdminFactory()
				.useDaemonThreads(true)
				.addLocators("${ip}:"+lusPort)
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
				"deploy-pu-schema":"partitioned-sync2backup",
				"deploy-pu-partitions":1,
				"deploy-pu-backups":0,
				"deploy-pu-maxpervm":1,
				"deploy-pu-maxpermachine":1,
				"deploy-pu-puname":(new File(puurl).name)
			])
		},
		//usage examples:
        //invoke xap-management  deploy-grid myIMDG partitioned-sync2backup 1 0 0 0 - deploy single space without backup
        //invoke xap-management  deploy-grid myIMDG partitioned-sync2backup 1 1 0 0 - deploy primary backup space
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
        //usage examples:
        //invoke xap-management  deploy-grid-basic myIMDG - deploy primary backup space
        "deploy-grid-basic"	: {name->
            util.invokeLocal(context,"_deploy-grid", [
                "deploy-grid-name":name,
                "deploy-grid-schema":"partitioned-sync2backup",
                "deploy-grid-partitions":1,
                "deploy-grid-backups":1,
                "deploy-grid-maxpervm":0,
                "deploy-grid-maxpermachine":0
            ])
        },
        //usage examples:
        //invoke xap-management  undeploy-grid myIMDG - undeploy space
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
    network {
        template "APPLICATION_NET"
        accessRules {
            incoming ([
                    accessRule {
                        type "PUBLIC"
                        portRange 8099
                    },
                    accessRule {
                        type "PUBLIC"
                        portRange 9099
                    },
                    accessRule {
                        type "APPLICATION"
                        portRange "4242-4342"
                    },
                    accessRule {
                        type "PUBLIC"
                        portRange 8080
                    }
            ])
        }
    }
}