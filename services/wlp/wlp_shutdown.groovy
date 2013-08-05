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
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

def config = new ConfigSlurper().parse(new File("wlp-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()
def instanceID = context.getInstanceId()

println "wlp_shutdown.groovy: Uninstalling websphere liberty server..."

serviceDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}"+instanceID
println "service dir is ${serviceDir} Uninstalling WAS 8.5 liberty"
new AntBuilder().sequential {
	delete(dir:"${serviceDir}")
}
println "wlp_shutdown.groovy: wlp ${config.serverName} Uninstalled "
