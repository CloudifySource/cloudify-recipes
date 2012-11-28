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
	
	name "solr"
	type "NOSQL_DB"
	icon "solr.png"

	lifecycle{
		install "solr_install.groovy"
		start "solr_start.groovy"
	}
	
	plugins([
		plugin {
			name "portLiveness"
			className "org.cloudifysource.usm.liveness.PortLivenessDetector"
			config ([
						"Port" : [8983],
						"TimeoutInSeconds" : 60,
						"Host" : "127.0.0.1"
					])
		},
		plugin {
			name "jmx"
			className "org.cloudifysource.usm.jmx.JmxMonitor"
			config([
						"Average Requests PerSecond": [
							"solr/:type=/admin/threads,id=org.apache.solr.handler.admin.ThreadDumpHandler",
							"avgRequestsPerSecond"
						],
						port: 9999
					])
		}
	])

	userInterface {

		metricGroups = ([
			metricGroup {
				name "Requests"
				metrics([
					"Average Requests PerSecond"
				])
			} ,
		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Average Requests PerSecond"
				widgets ([
					barLineChart{
						metric "Average Requests PerSecond"
						axisYUnit Unit.REGULAR
					}
				])
			},
		]
		)
	}
}


