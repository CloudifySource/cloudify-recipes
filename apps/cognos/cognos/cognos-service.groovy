/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
	name "cognos"
	type "DATABASE"
	icon "cognos.gif"
	elastic true	
  	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 4
	
	compute {
		template "SMALL_LINUX"
	} 

	lifecycle{
        
		preInstall "${context.serviceDirectory}/cognos_preInstall.sh ${context.serviceDirectory} ${congosDownloadUrl} ${congosFile} ${componentType} \"${yumPackages}\""

		postInstall "${context.serviceDirectory}/cognos_postInstall.sh ${context.serviceDirectory}"
		
		
		start "${context.serviceDirectory}/cognos_start.sh ${context.serviceDirectory}"
		startDetectionTimeoutSecs 1800

		locator {
           NO_PROCESS_LOCATORS
        }
		
		preStop ([        
			"Linux.*": "cognos_stop.sh"
        ])
		shutdown ([                        
			"Linux.*": "cognos_uninstall.sh"
        ])
	} 
}

