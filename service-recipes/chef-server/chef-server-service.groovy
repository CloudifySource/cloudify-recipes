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
import static Shell.*

service {
    extend "../chef"
	name "chef-server"
	type "WEB_SERVER"
	numInstances 1
    compute {
        template "MEDIUM_LINUX_CHEF_SERVER"
    }
	lifecycle{
        start "chef_server.groovy"
		
		startDetectionTimeoutSecs 600
		startDetection {
			ServiceUtils.isPortOccupied(4000)
		}
		
		
		details {
			def publicIp = System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			def serverRestUrl = "https://${publicIp}:443"
			def serverUrl = "http://${publicIp}:4000"
    		return [
    			"Rest URL":"<a href=\"${serverRestUrl}\" target=\"_blank\">${serverRestUrl}</a>",
    			"Server URL":"<a href=\"${serverUrl}\" target=\"_blank\">${serverUrl}</a>"
    		]
    	}    	
    	
    	
		locator {
			//hack to avoid monitoring started processes by cloudify
			return  [] as LinkedList			
		}
	}

}
