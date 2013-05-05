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
import static Shell.*
import static ChefLoader

service {
    extend "../chef"
    name "chef-server"
    type "WEB_SERVER"
    icon "chef.png"
    numInstances 1

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle{
        start {
            def bootstrap = ChefBootstrap.getBootstrap(installFlavor:"gem", context:context)
            def config = bootstrap.getConfig()
            bootstrap.runSolo(
                ["recipe[build-essential]", "recipe[chef-server::rubygems-install]", "recipe[chef-server::apache-proxy]" ], 
                [
                    "chef_server": [
                        "server_url": "http://localhost:4000",
                        "init_style": "${config.initStyle}"
                    ],
                    "chef_packages": [
                        "chef": [
                            "version": "${config.version}"
                        ]
                    ]
                ]
            )

            //setting the global attributes to be available for all chef clients  
            def privateIp = context.privateAddress
            def serverUrl = "http://${privateIp}:4000" as String
            context.attributes.global["chef_validation.pem"] = sudoReadFile("/etc/chef/validation.pem")
            context.attributes.global["chef_server_url"] = serverUrl
            return null
        }
        
        startDetectionTimeoutSecs 600
        startDetection {
            ServiceUtils.isPortOccupied(4000)
        }
        
        details {
            def publicIp = context.publicAddress
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

    // TODO: improve updateCookbooks with a try/catch (sh can throw an exception that is not printed)
    // TODO: improve listCookbooks and knife to print eventual error (shellout prints no error)
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
        },
        "listCookbooks": {
            chef_loader = ChefLoader.get_loader()
            return chef_loader.listCookbooks()
        },
        "knife": { String... args=[] ->
            chef_loader = ChefLoader.get_loader()
            return chef_loader.invokeKnife(args)
        }
    ])
}
