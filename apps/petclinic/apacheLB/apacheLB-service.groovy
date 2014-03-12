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
	extend "../../../services/apacheLB"	
	network {		
		protocolDescription = "HTTP"
		template "APPLICATION_NET"
		accessRules {[
			incoming ([
				accessRule {
					type "PUBLIC"
					portRange "1-40000"
					target "0.0.0.0/0"
				},
				accessRule {
					type "APPLICATION"
					portRange "1-40000"
					target "0.0.0.0/0"
                }				
			]),
			outgoing ([
				accessRule {
					type "PUBLIC"
					portRange "1-40000"
					target "0.0.0.0/0"
				},
				accessRule {
					type "APPLICATION"
					portRange "1-40000"
					target "0.0.0.0/0"
                }				
			])
		  ]
		}
	}
}