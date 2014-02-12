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
 ****************************************************************************** */

import java.util.concurrent.TimeUnit
import ChefBootstrap

service {
    name = "chef"

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle {
        init {
            // runParams can have been set by a custom command before init (self-healing...)
            // So we persist the dynamic configuration into Cloudify attributes.
            persistedRunParams = context.attributes.thisInstance.containsKey("runParams") ? context.attributes.thisInstance["runParams"] : [:]
            defaultRunParams = binding.variables.containsKey("runParams") ? binding.getVariable("runParams") : [run_list: "role[${context.serviceName}]" as String]
            runParams = defaultRunParams + persistedRunParams
            context.attributes.thisInstance["runParams"] = runParams
        }
        install {
            ChefBootstrap.getBootstrap(context: context).install() // default installation method defined in chef-service.properties
        }
        start {
            def chefServerURL = context.attributes.thisApplication["chef_server_url"]
            def validationCert = context.attributes.thisApplication["chef_validation.pem"]

            if (!chefServerURL) {
                def chefConfig = context.attributes.thisInstance["chefConfig"]
                chefServerURL = chefConfig.serverURL
                validationCert = chefConfig.validationCert
			}

            if (!chefServerURL) {
				chefServerURL = context.attributes.global["chef_server_url"]
				validationCert = context.attributes.global["chef_validation.pem"]
				if (!chefServerURL) {
					throw new RuntimeException("Cannot find a chef server URL in thisApplication nor in global attribute 'chef_server_url'")
				}							
            }

            println "Using Chef server URL: ${chefServerURL}"

            def runParamsLocal = context.attributes.thisInstance.containsKey("runParams") ? context.attributes.thisInstance["runParams"] : [run_list: "role[${context.serviceName}]" as String]

            ChefBootstrap.getBootstrap(
                    serverURL: chefServerURL,
                    validationCert: validationCert,
                    context: context
            ).runClient(runParamsLocal)
            println "End of service start"
            return null
        }

        locator {
            //hack to avoid monitoring started processes by cloudify
            return [] as LinkedList
        }
    }

    customCommands([
            "rerun": {
                bootstrap = ChefBootstrap.getBootstrap(context: context)
                runParamsLocal = context.attributes.thisInstance.containsKey("runParams") ? context.attributes.thisInstance["runParams"] : [run_list: "role[${context.serviceName}]" as String]
                bootstrap.runClient(runParamsLocal)
            },
            "run_apply" : {inlineRecipe ->
                bootstrap = ChefBootstrap.getBootstrap(context: context)
                bootstrap.runApply(inlineRecipe)
            },
            "run_chef": {serviceRunList = "role[${context.serviceName}]", chefType = "client", cookbookUrl = "" ->

                serviceRunList = serviceRunList.split(",").collect() { it.stripIndent() }
                if (cookbookUrl == "") {
                    cookbookUrl = null
                }
                output = "Beginning Chef run by custom command: chefType => ${chefType}, " +  \
                  "serviceRunList => ${serviceRunList}, cookbookUrl => ${cookbookUrl}"

                bootstrap = ChefBootstrap.getBootstrap(context: context)
                try { //hack - to see the error text, we must exit successfully(CLOUDIFY-915)
                    switch (chefType) {
                        case "client":
                            bootstrap.runClient([run_list: serviceRunList])
                            break

                        case "solo":
                            bootstrap.runSolo([run_list: serviceRunList], cookbookUrl)
                            break

                        default:
                            throw new Exception("Unrecognized chefType(${chefType}), please use 'client' or 'solo'")
                    }
                } catch (Exception e) {
                    output += "Chef client run encountered an exception:\n${e}"
                }
                println output //goes to the gsc log
            },

            "run_cucumber": "run_cucumber.sh"
    ])
}
