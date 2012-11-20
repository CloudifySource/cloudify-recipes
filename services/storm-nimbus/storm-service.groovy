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

	name "storm-nimbus"
	type "APP_SERVER"
	icon "storm.png"
	elastic false
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 1

    compute {
        template "SMALL_LINUX"
    }

	lifecycle{
		init "storm_install.groovy"
		start "storm_start.groovy"
		preStop "storm_stop.groovy"
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
		"wordcount-start": "commands/wordcount-start.sh"
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


