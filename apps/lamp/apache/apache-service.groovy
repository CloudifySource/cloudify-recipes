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
	extend "../../../services/apache"	
	lifecycle {
		postInstall "apache_installSample.groovy"
		
		details {
			def publicIP
			
			if (  context.isLocalCloud()  ) {
				publicIP =InetAddress.getLocalHost().getHostAddress()
			}
			else {
				publicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
				
			def hostAndPort="http://${publicIP}:${port}"			
			def phpInfo = "${hostAndPort}/${phpInfo}"														
			def sampleURL = "${hostAndPort}/${sample}"
			def hangonmanURL = "${hostAndPort}/${hangonman}"
		
			return [				
				"Sample MySQL":"<a href=\"${sampleURL}\" target=\"_blank\">${sampleURL}</a>",					
				"phpinfo":"<a href=\"${phpInfo}\" target=\"_blank\">${phpInfo}</a>",
				"HangOnMan":"<a href=\"${hangonmanURL}\" target=\"_blank\">${hangonmanURL}</a>",					
			]		
		}	
	}		
}