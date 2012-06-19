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
import java.net.InetAddress 

service {
    extend "../../../services/chef"
    name "mysql"
    type "DATABASE"
    icon "mysql.png"
    numInstances 1
    
    
    lifecycle {
    	startDetectionTimeoutSecs 240
		startDetection {
			ServiceUtils.isPortOccupied(System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"], 3306)
		}		       	
    }
    
    compute {
        template "MEDIUM_LINUX"
    }
}
