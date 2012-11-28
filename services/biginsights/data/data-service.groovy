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
	name "data"
	icon "biginsights.png"
	numInstances 1
	
	compute {
		template "DATA"
	}
		
	lifecycle {
		install "data_install.groovy"
		start "data_start.groovy" 		
		preStop "data_stop.sh"
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
		
    network {
        port = dataNodePort
        protocolDescription ="DataNode"
    }
        	
}
