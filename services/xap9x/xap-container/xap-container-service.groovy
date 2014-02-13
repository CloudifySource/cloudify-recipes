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
	def maxinstances=context.isLocalCloud()?1:200
	println maxinstances

	name "xap-container"
	type "APP_SERVER"
	icon "xap.png"
	elastic true
	numInstances containerCount
	minAllowedInstances 1
	maxAllowedInstances maxinstances


    compute {
        template "${template}"
    }

	def instanceId=context.instanceId

	lifecycle{


		install "xap_install.groovy"

		start "xap_start.groovy"

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
				i=i+1
			}
			if(i>11)return null

			i=0
			def pids=[]
			while(pids.size()==0){
				pids=ServiceUtils.ProcessUtils.getPidsWithQuery("Args.*.ct=${uuid}");
				i++;
				if(i>10){
					println "PROCESS NOT DETECTED"
					break
				}
				Thread.sleep(1000)
			}
			return pids
		}
	}


	customCommands ([
//Public entry points

		"update-hosts": {String...line ->
			util.invokeLocal(context,"_update-hosts", [
				"update-hosts-hostsline":line
			])
		 },


		//Actual parameterized calls
		"_update-hosts"	: "commands/update-hosts.groovy"

	])


	userInterface {
		metricGroups = ([
		]
		)

		widgetGroups = ([
		]
		)
	}
}


