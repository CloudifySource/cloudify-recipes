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

def static addOrRemoveNode(config,lifecycleEvent,actionName,seconds2Wait,context,currHttpPort,instanceID) { 
		
	def myServiceName = config.serviceName		
	try {			
		def loadBalancerServiceName = config.loadBalancerServiceName	
		println "${myServiceName}: In ${lifecycleEvent} ..."
		def loadbalancerService = context.waitForService(loadBalancerServiceName, seconds2Wait, TimeUnit.SECONDS)			
		if ( loadbalancerService != null ) { 
			println "${myServiceName}: Invoking ${actionName} of ${loadBalancerServiceName} ..."
					
			def privateIP
			if (  context.isLocalCloud()  ) {
				privateIP=InetAddress.getLocalHost().getHostAddress()
			}
			else {
				privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
			}
			println "${myServiceName}: privateIP is ${privateIP} ..."
			
			def currURL="http://${privateIP}:${currHttpPort}/${config.ctxPath}"
			println "${myServiceName}: About to ${actionName} ${currURL} to ${loadBalancerServiceName} ..."
			loadbalancerService.invoke("${actionName}", currURL as String, instanceID as String)
		}		
		else {
			println "${myServiceName}: waitForService returned ${loadBalancerServiceName} null"	
		}
	}
	catch (all) {		
		println "${myServiceName}: Exception in addOrRemoveNode: " + all		
	}
	println "${myServiceName}: ${lifecycleEvent} ended"

}
		
