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

service { 
    extend "../groovy-utils"
    name "puppet"
    icon "puppet.png" //eventually we should probably only use it for the master

    lifecycle {
      install {
        bootstrap = PuppetBootstrap.getBootstrap(context:context)
        bootstrap.install()

        //TODO: pull additional properties from config?
        manifestsType = "tar" // Or git or svn
        manifestsUrl = "http://fewbytes-development.s3.amazonaws.com/clients/gigaspaces/manifests.tgz"
        bootstrap.loadManifest(manifestsType, manifestsUrl)
        bootstrap.appplyManifest()
      } 
      start {
        bootstrap = PuppetBootstrap.getBootstrap(context:context)
        bootstrap.appplyManifest()
      }
    }

    customCommands([
      "run_puppet": {manifestOriginType, manifestOriginUrl ->
        bootstrap = PuppetBootstrap.getBootstrap(context:context)
        try{ //hack - to see the error text, we must exit successfully(CLOUDIFY-915)
            bootstrap.loadManifest(manifestsType, manifestsUrl)
            bootstrap.appplyManifest()
        } catch(Exception e) {
          println "Puppet agent run encountered an exception:\n${e}" //goes to the gsc log
        }
      },
    ])   
}
