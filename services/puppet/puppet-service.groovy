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
 ****************************************************************************** */

import java.util.concurrent.TimeUnit
import PuppetBootstrap
import static Shell.*

service { 
    extend "../groovy-utils"
    name "puppet"
    icon "puppet.png"

    lifecycle {
      install {
        bootstrap = PuppetBootstrap.getBootstrap(context:context)
        bootstrap.install()
      }
      start {
        bootstrap = PuppetBootstrap.getBootstrap(context:context)
        if (binding.variables["puppetRepo"]) {
            bootstrap.loadManifest(puppetRepo.repoType, puppetRepo.repoUrl)
            if (puppetRepo.manifestPath) {
                bootstrap.appplyManifest(puppetRepo.manifestPath)
            } else if (puppetRepo.classes) {
                bootstrap.applyClasses(puppetRepo.classes)
            } else {
                println "Puppet repository loaded but nothing to run."
            }

        } else {
            println "Puppet repository undefined in the properties file."
        }
      }
    }

    customCommands([
        "run_puppet": {repoType, repoUrl, manifestPath=null ->
            bootstrap = PuppetBootstrap.getBootstrap(context:context)
            try{ //hack - to see the error text, we must exit successfully(CLOUDIFY-915)
                bootstrap.loadManifest(repoType, repoUrl)
                bootstrap.appplyManifest(manifestPath)
            } catch(Exception e) {
              println "Puppet agent run encountered an exception:\n${e}" //goes to the gsc log
            }
        },
        "cleanup_repo": { 
            bootstrap = PuppetBootstrap.getBootstrap(context:context)
            bootstrap.cleanup_local_repo()
        }
    ])   
}
