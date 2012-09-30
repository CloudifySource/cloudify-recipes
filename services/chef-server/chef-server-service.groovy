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
import static Shell.*
import static ChefLoader

service {
    extend "../chef"
	name "chef-server"
	type "WEB_SERVER"
	icon "chef.png"
	numInstances 1

    compute {
        template "MEDIUM_UBUNTU"
    }

	lifecycle{
        start {
            def bootstrap = ChefBootstrap.getBootstrap(installFlavor:"gem", context:context)
            def config = bootstrap.getConfig()
            bootstrap.runSolo([
                "chef_server": [
                    "server_url": "http://localhost:8080",
                    "init_style": "${config.initStyle}"
                ],
                "chef_packages": [
                    "chef": [
                        "version": "${config.version}"
                    ]
                ],
                "run_list": ["recipe[build-essential]", "recipe[chef-server::rubygems-install]", "recipe[chef-server::apache-proxy]" ]
            ])

            //setting the global attributes to be available for all chef clients  
            def privateIp = System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
            def serverUrl = "http://${privateIp}:4000" as String
            context.attributes.global["chef_validation.pem"] = sudoReadFile("/etc/chef/validation.pem")
            context.attributes.global["chef_server_url"] = serverUrl
        }
		
		startDetectionTimeoutSecs 600
		startDetection {
			ServiceUtils.isPortOccupied(4000)
		}
		details {
			def publicIp = System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			def serverRestUrl = "https://${publicIp}:443"
			def serverUrl = "http://${publicIp}:4000"
    		return [
    			"Rest URL":"<a href=\"${serverRestUrl}\" target=\"_blank\">${serverRestUrl}</a>",
    			"Server URL":"<a href=\"${serverUrl}\" target=\"_blank\">${serverUrl}</a>"
    		]
    	}
        postStart { 
            if (binding.variables["chefRepo"]) {
                chef_loader = ChefLoader.get_loader(chefRepo.repo_type)
                chef_loader.initialize()
                chef_loader.fetch(chefRepo.url, chefRepo.inner_path)
                chef_loader.upload()
            } else {
                ChefLoader.get_loader().initialize()
            }
        }
    }

    customCommands([
        "updateCookbooks": { repo_type="git",
                             url="by default, the existing repo will be reused",
                             inner_path=null ->
            chef_loader = ChefLoader.get_loader(repo_type) 
            chef_loader.fetch(url, inner_path)
            chef_loader.upload()
        },
        "cleanupCookbooks": { 
            chef_loader = ChefLoader.get_loader() 
            chef_loader.cleanup_local_repo()
        }
    ])
}
