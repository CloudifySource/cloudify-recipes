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

import java.util.concurrent.TimeUnit

service {
    lifecycle {
      install {
      	ChefBootstrap.getBootstrap(context:context, installFlavor: "gem").install()
      } 
		  start "run_chef.groovy"

		  locator {
			  //hack to avoid monitoring started processes by cloudify
			  return  [] as LinkedList			
		  }
    }

    customCommands([
        "run_chef": {serviceRunList="role[${context.serviceName}]", chefType="client" ->
          println "Runing Chef by custom command: chefType => ${chefType}, serviceRunList => ${serviceRunList}"
          serviceRunList = serviceRunList.split(",").collect(){ it.stripIndent() }
          ChefBootstrap.getBootstrap(
            context:context
          )."run${chefType.capitalize()}"([run_list: serviceRunList])
        },
        "run_cucumber": "run_cucumber.sh"
    ])
}
