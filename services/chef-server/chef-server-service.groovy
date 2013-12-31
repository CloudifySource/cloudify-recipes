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
import static Shell.*
import static ChefLoader
import groovy.json.JsonOutput

service {
    extend "../chef"
	name "chef-server"
	type "WEB_SERVER"
	icon "chef.png"
	numInstances 1

    compute {
		// Chef server does NOT support 32bit !!!
        template "SMALL_UBUNTU"
    }

	lifecycle{
        start {
            def ipAddress = context.privateAddress
            if (ipAddress == null || ipAddress.trim() == "") ipAddress = context.publicAddress
            def serverUrl = "https://${ipAddress}:443" as String
            def bootstrap = ChefBootstrap.getBootstrap(installFlavor:"fatBinary", context:context)

            def chefServerConfig = [:]
            // If the chef-server isn't externally routable, we need to use privateIp
            // (https://tickets.opscode.com/browse/CHEF-4086)
            if (!externallyRoutableHostname) { // see properties file
                chefServerConfig["bookshelf"] = ["url": serverUrl ]
                chefServerConfig["nginx"]     = ["url": serverUrl ]
                chefServerConfig["erchef"]    = ["s3_url_ttl": "21600"]
            }

            bootstrap.runSolo([
                "chef-server": [
                    "version": "${chefServerVersion}", // defined in properties file
                    "configuration": chefServerConfig
                ],
                "run_list": ["recipe[chef-server]"]
            ])

            //setting the thisApplication attributes to be available for all chef clients in this application
            context.attributes.thisApplication["chef_validation.pem"] = sudoReadFile("/etc/chef-server/chef-validator.pem")
            context.attributes.thisApplication["chef_server_url"] = serverUrl
        }

		startDetectionTimeoutSecs 600
		startDetection {
			ServiceUtils.isPortOccupied(443)
		}

		details {
			def publicIp = System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			def serverRestUrl = "https://${publicIp}:443"
			def serverUrl = "https://${publicIp}:443"
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
        },
        "listCookbooks": {
            chef_loader = ChefLoader.get_loader()
            return chef_loader.listCookbooks()
        },
        "cleanupNode": { String node_name ->
            chef_loader = ChefLoader.get_loader()
            chef_loader.invokeKnife(["node", "delete", node_name, "-y"])
            chef_loader.invokeKnife(["client", "delete", node_name, "-y"])
            return "${node_name} cleaned up"
        },
        "createNode": { String node_name ->
            chef_loader = ChefLoader.get_loader()
								
            def jsonFileContent = "{ \"name\": \"${node_name}\", \"chef_environment\": \"_default\", \"json_class\": \"Chef::Node\", \"automatic\": { }, \"normal\": { }, \"chef_type\": \"node\", \"default\": { }, \"override\": { }, \"run_list\": [ ] }"
            def currentNodeJsonFile = "/tmp/currentNode.json"
            sudoWriteFile( "${currentNodeJsonFile}" ,jsonFileContent)
				
            def createNodeArgs = [ "node" , "from", "file", "${currentNodeJsonFile}" ]
            println "creating Node ${node_name} : knife node from file ${currentNodeJsonFile} ..."
            return chef_loader.invokeKnife(createNodeArgs)
        },
        "knife": { String... args=[] ->
            chef_loader = ChefLoader.get_loader()
            return chef_loader.invokeKnife(args)
        }


    ])
}
