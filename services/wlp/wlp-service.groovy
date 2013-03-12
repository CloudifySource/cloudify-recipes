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
	name "wlp"
	icon "wlp.png"
	type "APP_SERVER"
	
    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2
	
	compute {
		template "SMALL_LINUX"
	}

lifecycle{
  install "wlp_install.groovy"
  postInstall "wlp_postinstall.groovy"
  start "wlp_start.groovy"
  stop "wlp_stop.groovy"
  shutdown "wlp_shutdown.groovy"
  startDetectionTimeoutSecs 200
  startDetection {
			println "startDetection: Testing port ${wlpPort} ..."
			ServiceUtils.isPortOccupied(wlpPort)							
		}   
  locator {
           def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.eq=${serverName}")
           println ":current PIDs: ${myPids}"
           return myPids
        }	
}
}