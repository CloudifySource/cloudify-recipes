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

service {
	extend "../../../services/biginsights/data"

	name "dataOnDemand"

	icon "biginsights.png"

    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 3

	

	compute {

		template "SMALL_LINUX"

	}

		

	lifecycle {

		install "data_install.groovy"

		start "data_start.groovy" 	
		def instanceID = context.instanceId	
		postStart {
			println "dataOnDemand-service.groovy: dataOnDemand Post-start ..."
			def masterService = context.waitForService("master", 180, TimeUnit.SECONDS)			
			sleep(30000)
			def privateIP
			privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
			println "dataOnDemand-service.groovy: privateIP is ${privateIP} ..."
			masterService.invoke("addNode", privateIP as String, "hadoop", instanceID as String)
			println "dataOnDemand-service.groovy: dataOnDemand Post-start ended"						
		}		

		preStop {
			println "dataOnDemand-service.groovy: dataOnDemand Pre-stop ..."
			def masterService = context.waitForService("master", 180, TimeUnit.SECONDS)			
			def privateIP
			privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
			println "dataOnDemand-service.groovy: privateIP is ${privateIP} ..."
			masterService.invoke("removeNode", privateIP as String, instanceID as String)
			println "dataOnDemand-service.groovy: dataOnDemand Pre-stop ended"
			new AntBuilder().sequential {	
				chmod(file:"${context.serviceDirectory}/data_stop.sh", perm:"ugo+rx")
				exec(executable:"${context.serviceDirectory}/data_stop.sh", osfamily:"unix", failonerror:"false") {
				}
			}
		}

		startDetectionTimeoutSecs 3000	

		startDetection {

			ServiceUtils.isPortOccupied(22)

		}	

		locator {			

			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.eq=org.apache.hadoop.hdfs.server.datanode.DataNode")

			println ":data-service.groovy: current PIDs: ${myPids}"

			return myPids

        }					

	}

		
}