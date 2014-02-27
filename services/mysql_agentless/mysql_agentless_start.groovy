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
import org.cloudifysource.domain.cloud.compute.ComputeTemplate;
import org.cloudifysource.esc.driver.provisioning.MachineDetails_;

String OPENSTACK_KEY_PAIR = "openstack.keyPair";
String OPENSTACK_SECURITYGROUP = "openstack.securityGroup";

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
			context.attributes.thisInstance["token"] = token

			try{
				ComputeTemplate ct = new ComputeTemplate();
				ct.setAbsoluteUploadDir(config.folder);
				Map<String,Object> options = new HashMap<String,Object>();
				options.put(OPENSTACK_KEY_PAIR, config.keypair);
				options.put(OPENSTACK_SECURITYGROUP, config.securitygroup);
				options.put("config_drive", config.config_drive);

/* Example of placing a file
				options.put("full_path", "/tmp/testfile");
				options.put("content", "234567898765432345678987654345678987654323456787654345678976543456787654345675");
				options.put("user-data", context.serviceDirectory + "/" + config.script)
*/
				ct.setOptions(options);
				ct.setHardwareId(config.hardwareId);
				ct.setImageId(config.imageId);
				ct.setKeyFile(config.key_file);
				
				MachineDetails_ md = nova.newServer(token,System.currentTimeMillis() +  1000*60*5, ct);
				context.attributes.thisInstance["machineId"] = md.getMachineId()
				println(md.getMachineId());
				println(md.getPrivateAddress());
				context.attributes.thisInstance["privateip"] = md.getPrivateAddress()
				println(md.getPublicAddress());
				context.attributes.thisInstance["publicip"] = md.getPublicAddress()
			}catch(Exception e)
			{
				e.printStackTrace();
			}



