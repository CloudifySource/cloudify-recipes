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

	name "zookeeper"
	type "DATABASE"
	icon "zookeeper-small.jpg"
	numInstances 1 
	maxAllowedInstances 3       //currently only 1 instance supported
	minAllowedInstances 1

	compute {
		template "SMALL_LINUX"
	}

	lifecycle{
		install "zookeeper_install.groovy"
		start "zookeeper_start.groovy"
		stop "zookeeper_stop.groovy"
		locator {
			ServiceUtils.ProcessUtils.getPidsWithMainClass("org.apache.zookeeper.server.quorum.QuorumPeerMain")
		}
		monitors{
			def dir=context.serviceDirectory
			def metrics=[]
			if(ServiceUtils.isLinuxOrUnix()){
				def process="${context.serviceDirectory}/stat.sh ${clientPort}".execute()
				process.in.eachLine{line-> 
					metrics.add line
				}
				[       "Packets Received":metrics[0],
					"Packets Sent":metrics[1],
					"Outstanding Requests":metrics[2]
				]
			}
		}

	}
	plugins([
		plugin {
			name "portLiveness"
			className "org.cloudifysource.usm.liveness.PortLivenessDetector"
			config ([
						"Port" : [clientPort],
						"TimeoutInSeconds" : 60,
						"Host" : "127.0.0.1"
					])
		}
	])

	userInterface {
		metricGroups = ([
			metricGroup {

				name "server"

				metrics([
					"Outstanding Requests",
					"Packets Received",
					"Packets Sent",
				])
			},
		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Outstanding Requests"
				widgets ([
					barLineChart{
						metric "OutStanding Requests"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Packets Received"
				widgets ([
					barLineChart{
						metric "Packets Received"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Packets Sent"
				widgets ([
					barLineChart{
						metric "Packets Sent"
						axisYUnit Unit.REGULAR
					}
				])
			},
		]
		)
	}
}


