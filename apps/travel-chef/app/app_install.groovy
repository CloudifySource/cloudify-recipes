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

import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def context = ServiceContextFactory.getServiceContext()
def chef_server_service = context.waitForService("chef-server", 20, TimeUnit.SECONDS)
def chefServerURL = "http://${chef_server_service.instances[0].hostName}:4000".toString()
def validationCert = context.attributes.thisApplication["chef_validation.pem"]

println "Using Chef server URL: ${chefServerURL}"

ChefBootstrap.getBootstrap(
    serverURL: chefServerURL,
    validationCert: validationCert,
    context: context
).runClient([
	"tomcat": ["java_options": "-Dcom.sun.management.jmxremote.port=11099 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"],
    "run_list": ["role[${context.serviceName}]".toString()]
])
