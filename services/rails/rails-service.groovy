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
import RailsBootstrap
import static Shell.*

service { 
    extend "../groovy-utils"
    name "rails"
    icon "rails.png"

    elastic true
    numInstances 1
    minAllowedInstances 1
    maxAllowedInstances 2

    lifecycle {
        install {
            if (! binding.variables["webappOpts"])
                throw new Exception("Rails application options were not specified - \
                                     please add the webappOpts property")

            bootstrap = RailsBootstrap.getBootstrap(context:context, webappOpts:webappOpts)
            bootstrap.install()

            bootstrap.runHook({
                rubySh("bundle exec rake generate_session_store")
                rubySh("bundle exec rake redmine:load_default_data REDMINE_LANG=en")
            })

            bootstrap.migrate()

            bootstrap.runHook({
              print "This could be a postMigrateHook"
            })
        }

        start {
          bootstrap = RailsBootstrap.getBootstrap(context:context, webappOpts:webappOpts)
          bootstrap.start() 

          bootstrap.runHook({
            print "This could be a postStartHook"
          })
        }

        postStart {
            def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
            def privateIP = System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
            def instanceID = context.instanceId
            def currURL="http://${privateIP}:8080"
            apacheService.invoke("addNode", currURL as String, instanceID as String)
        }

        postStop {
            try {
                def apacheService = context.waitForService("apacheLB", 180, TimeUnit.SECONDS)
                if ( apacheService != null ) {
                    def privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
                    def instanceID = context.instanceId
                    def currURL="http://${privateIP}:8080"
                    apacheService.invoke("removeNode", currURL as String, instanceID as String)
                }
            }
            catch (all) {
                println "app-service.groovy: Exception in Post-stop: ${all}"
            }
        }

      //TODO: add monitoring and autoscaling based on some rails metric taken from rack
    }
}
