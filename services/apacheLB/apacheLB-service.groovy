/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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
	
	name "apacheLB"
	icon "feather-small.gif"
	type "WEB_SERVER"
	numInstances 1

	

	lifecycle {
	
	
		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP =InetAddress.getLocalHost().getHostAddress()
			}
			else {
				currPublicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
			def loadBalancerURL	= "http://${currPublicIP}:${currentPort}"
			def balancerManagerURL = loadBalancerURL+"/balancer-manager"
			def applicationURL = loadBalancerURL+"/"+applicationName
			
				return [
					"BalancerManager URL":"<a href=\"${balancerManagerURL}\" target=\"_blank\">${balancerManagerURL}</a>",
					"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
				]
		}	
	
		install "apacheLB_install.groovy"
		postInstall "apacheLB_postInstall.groovy"
		start ([
			"Win.*":"run.bat",
			"Linux.*":"apacheLB_start.groovy"
			])
			
		startDetectionTimeoutSecs 800
		startDetection {			
			ServiceUtils.isPortOccupied(currentPort)
		}	
		
		preStop ([	
			"Win.*":"killAllHttpd.bat",		
			"Linux.*":"apacheLB_stop.groovy"
			])
		shutdown ([			
			"Linux.*":"apacheLB_uninstall.groovy"
		])
			
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.re=httpd|apache")
			println ":apacheLB-service.groovy: current PIDs: ${myPids}"
			return myPids
        }
			
			
	}
	
	customCommands ([
		"addNode" : "apacheLB_addNode.groovy",
		"removeNode" : "apacheLB_removeNode.groovy",
		
		/* In order to test your application under load, you can use this "load" custom command.
			It uses Apache Bench which is installed by default with apache.
   
   
			The following will fire 35000 requests on http://LB_IP_ADDRESS:LB_PORT/ with 100 concurrent requests each time:
				invoke apacheLB load 35000 100

			The following will fire 20000 requests on http://LB_IP_ADDRESS:LB_PORT/petclinic-mongo with 240 concurrent requests each time: 
				invoke apacheLB load 20000 240 petclinic-mongo
		*/
		"load" : "apacheLB-load.groovy"
	])
	
	
	network {
		port currentPort
		protocolDescription "HTTP"
	}
}
