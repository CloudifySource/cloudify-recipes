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

service {

	name "storm-supervisor"
	type "APP_SERVER"
	icon "storm.png"
	elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 100


    compute {
        template "MEDIUM_LINUX"
    }

	lifecycle{
		install "storm_install.groovy"
		postInstall "storm_postinstall.groovy"
		start "storm_start.groovy"
		preStop "storm_stop.groovy"
	}
	plugins([
	])

	customCommands([
		"addhostentry": { ip,hostname->
			if(!util.lockFile("/etc/hosts"))return false;
			try{
			println "running addhost.sh with args ${ip} ${hostname}";
			"chmod ugo+rwx ${context.serviceDirectory}/commands/addhost.sh".execute()
			"${context.serviceDirectory}/commands/addhost.sh ${ip} ${hostname}".execute()
			}
			catch(Exception e){
				e.printStackTrace()
				return false
			}
			finally{ util.unlockFile("/etc/hosts");}
			return true
			
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


