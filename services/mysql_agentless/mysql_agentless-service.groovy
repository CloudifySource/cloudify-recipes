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

	name "${name}"
	type "DATABASE"

	lifecycle{
		start "mysql_start.groovy"
		postStop "mysql_stop.groovy"
		startDetectionTimeoutSecs 240
		startDetection {
			def publicip = context.attributes.thisInstance["publicip"]
			def privateip = context.attributes.thisInstance["privateip"]
			println "mysql-service.groovy:(startDetection)"
			println "mysql-service.groovy:(${privateip},${port})"
			!ServiceUtils.isPortFree(privateip,port );
		}	
		stopDetection {
			def privateip = context.attributes.thisInstance["privateip"]
			ServiceUtils.isPortFree(privateip,port );
		}
		details {
			def publicip = context.attributes.thisInstance["publicip"]
			def privateip = context.attributes.thisInstance["privateip"]
			println "mysql-service.groovy: details:"
			println "mysql-service.groovy: server is ${publicip} port is ${port}"
		
            return [
                "Host port":"${port}",
                "Host public IP":"${publicip}",
                "Host private IP":"${privateip}",
                "Host Machine ID":context.attributes.thisInstance["machineId"],
                "Token":context.attributes.thisInstance["token"] 
            ]
		}	
	}

	customCommands ([       	
		"stop" : "mysql_stop.groovy"
	])

}


