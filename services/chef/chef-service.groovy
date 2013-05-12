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
 ****************************************************************************** */

import java.util.concurrent.TimeUnit
import ChefBootstrap

service {
    name = "chef"

    compute {
        template "SMALL_LINUX"
    }

    lifecycle {
        init {
            // Look for local configuration (external Chef server), then in Cloudify global attributes (Chef server deployed by Cloudify)
            def chefServerURL = binding.variables.get("chef_server_url") ? : context.attributes.global["chef_server_url"]
            def validationCert = binding.variables.get("chef_validation_pem") ? : context.attributes.global["chef_validation_pem"]

            if (chefServerURL == null) {
                throw new RuntimeException("Cannot find a chef server URL in global attribtue 'chef_server_url'")
            }
            println "Using Chef server URL: ${chefServerURL}"
            ChefBootstrap.getBootstrap(
                    serverURL: chefServerURL,
                    validationCert: validationCert,
            ).runClient(runParamsLocal)

            persistedRunParams = context.attributes.thisInstance.containsKey("runParams") ? context.attributes.thisInstance["runParams"] : [:]
            defaultRunParams = binding.variables.containsKey("runParams") ? binding.getVariable("runParams") : [:]
            // merge: defaults from properties file, then persisted config from attributes
            runParams = defaultRunParams + persistedRunParams
            // persist to context attributes (for a self-healing or a custom command)
            context.attributes.thisInstance["runParams"] = runParams
        }
        install {
            ChefBootstrap.getBootstrap(context: context).install() // default installation method defined in chef.properties
        }
        start {
            def runParamsLocal = context.attributes.thisInstance.containsKey("runParams") ? context.attributes.thisInstance["runParams"] : [:]
            ChefBootstrap.getBootstrap(context: context).runClient(runParamsLocal)
            return null
        }

        locator {
            //hack to avoid monitoring started processes by cloudify
            return [] as LinkedList
        }
    }

    customCommands([
            "rerun": {
                def runParamsLocal = context.attributes.thisInstance.containsKey("runParams") ? context.attributes.thisInstance["runParams"] : [:]
                ChefBootstrap.getBootstrap(context: context).runClient(runParamsLocal)
                return true
            },
            "runApply": {inlineRecipe ->
                ChefBootstrap.getBootstrap(context: context).runApply(inlineRecipe)
                return true
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
