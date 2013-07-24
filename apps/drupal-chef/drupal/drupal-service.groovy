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
    extend "../../../services/chef"
    name "drupal"
    type "WEB_SERVER"
    icon "drupal.jpg"
    
    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle {
		def httpPort = 80
		startDetectionTimeoutSecs 800
		startDetection {			
			ServiceUtils.isPortOccupied(httpPort)
		}	

		stopDetection {
			!(ServiceUtils.isPortOccupied(httpPort))
		}
				
		details {
			def currPublicIP =context.getPublicAddress()
			def drupalSiteHome = "http://${currPublicIP}:${httpPort}"
			def drupalInstallUrl = "${drupalSiteHome}/install.php"
    		return [
				"Drupal Install Url":"<a href=\"${drupalInstallUrl}\" target=\"_blank\">${drupalInstallUrl}</a>",
				"Drupal Site Home":"<a href=\"${drupalSiteHome}\" target=\"_blank\">${drupalSiteHome}</a>"    			
    		]
    	}    	
    }
}
