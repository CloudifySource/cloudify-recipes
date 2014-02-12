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
import static JmxMonitors.*

service {
	extend "../../../services/tomcat"
	elastic false
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 1

	def portIncrement =  context.isLocalCloud() ? context.getInstanceId()-1 : 0

	def currJmxPort = jmxPort + portIncrement
	def currHttpPort = port + portIncrement
	def currAjpPort = ajpPort + portIncrement

	lifecycle {

		details {
			def currPublicIP

			if (  context.isLocalCloud()  ) {
				currPublicIP =InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP = System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
			def tomcatURL = "http://${currPublicIP}:${currHttpPort}"

			def applicationURL = "${tomcatURL}/${ctxPath}"
			println "tomcat-service.groovy: applicationURL is ${applicationURL}"

			return [
				"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
			]
		}

		monitors {

			def metricNamesToMBeansNames = [
						"Current Http Threads Busy": ["Catalina:type=ThreadPool,name=\"http-bio-${currHttpPort}\"", "currentThreadsBusy"],
						"Current Http Thread Count": ["Catalina:type=ThreadPool,name=\"http-bio-${currHttpPort}\"", "currentThreadCount"],
						"Backlog": ["Catalina:type=ProtocolHandler,port=${currHttpPort}", "backlog"],
						"Total Requests Count": ["Catalina:type=GlobalRequestProcessor,name=\"http-bio-${currHttpPort}\"", "requestCount"],
						"Active Sessions": ["Catalina:type=Manager,context=/${ctxPath},host=localhost", "activeSessions"],
					]

			return getJmxMetrics("127.0.0.1",currJmxPort,metricNamesToMBeansNames)
		}
	}
	
	network {
		port = currHttpPort
		protocolDescription = "HTTP"
		template "APPLICATION_NET"
		accessRules {
			incoming ([
				accessRule {
					type "PUBLIC"
					portRange currHttpPort
					target "0.0.0.0/0"
				}
			])
		}
	}
}
