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

	name "nginx"
	icon "nginx-logo.png"
	type "WEB_SERVER"

	lifecycle{
		init "nginx_install.groovy"
		start "nginx_start.groovy"
		preStop "nginx_stop.groovy"
	}

	plugins([
		plugin {
			name "portLiveness"
			className "org.cloudifysource.usm.liveness.PortLivenessDetector"
			config ([
						"Port" : [8000],
						"TimeoutInSeconds" : 60,
						"Host" : "127.0.0.1"
					])
		}
	])

	userInterface {

		metricGroups = ([
		]
		)

		widgetGroups = ([]
		)
	}
}
