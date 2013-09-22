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
import org.cloudifysource.openstack.OpenstackNova;
import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("mysql-service.properties").toURL())

			OpenstackNova nova = new OpenstackNova();
			nova.setTenant(config.tenant);
			
			nova.setUser(config.user);
			nova.setApiKey(config.apiKey);
			nova.setEndpoint(config.endpoint);
			nova.setIdentityEndpoint(config.identityEndpoint);
			nova.setPathPrefix("v1.1/" +  config.tenant + "/");
			String token = nova.createAuthenticationToken();
			nova.setPathPrefix("v1.1/" +  config.tenant + "/");
			def machineId = context.attributes.thisInstance["machineId"]
			nova.terminateServer(machineId, token, System.currentTimeMillis()+1000*60*5);
