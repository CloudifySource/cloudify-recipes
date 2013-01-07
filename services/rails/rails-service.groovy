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

import RailsBootstrap
import static Shell.*

service { 
    extend "../groovy-utils"
    name "rails"
    icon "rails.png"

    lifecycle {
      install {
        if (binding.variables["preInstallHook"]) {
            preInstallHook(context:context) //TODO: should we pass any additional arguments?
        }

        bootstrap = RailsBootstrap.getBootstrap(context:context)
        if (binding.variables["webappRepo"]) {
            bootstrap.install(webappRepo)
        } else {
            throw new Exception("Rails application was not specified - add the webappRepo property")
        }

        if (binding.variables["postInstallHook"]) {
            postInstallHook(context:context) //TODO: should we pass any additional arguments?
        }
      }

      start {
        bootstrap = RailsBootstrap.getBootstrap(context:context)

        print "This would be where the server is started"
        //bootstrap.start() 

        if (binding.variables["postStartHook"]) {
            postStartHook(context:context) //TODO: should we pass any additional arguments?
        }
      }
    }
}
