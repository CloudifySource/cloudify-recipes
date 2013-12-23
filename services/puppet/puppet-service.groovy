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
import PuppetBootstrap
import groovy.json.JsonSlurper
import static Shell.*

/*
import org.cloudifysource.utilitydomain.context.ServiceImpl

def waitForGlobalService(puppetApplicationName, puppetMasterServiceName, timeout, unit) {
   puName = ServiceUtils.getAbsolutePUName(puppetApplicationName, puppetMasterServiceName)
   ServiceImpl(context.admin.getProcessingUnits().waitFor(puName, timeout, unit))
}
*/

service { 
    extend "../groovy-utils"
    name "puppet"
    icon "puppet.png"

    lifecycle {
      install {
        puppetEnvironment = context.attributes.application["puppet_environment"] ?: binding.variables.get("puppetEnvironment") ?: context.applicationName
		println "puppet-service.groovy.install: puppetEnvironment is ${puppetEnvironment}"
        puppetMasterIp = context.attributes.global["puppet_master_ip"] ?: binding.variables.get("puppetMasterIp")
		println "puppet-service.groovy.install: puppetMasterIp is ${puppetMasterIp}"
        puppetNodePrefix = binding.variables.get("puppetNodePrefix") ?: context.attributes.global["puppetNodePrefix"]
		println "puppet-service.groovy.install: puppetNodePrefix is ${puppetNodePrefix}"
        domainName = binding.variables.get("domainName") ?: context.attributes.global["domainName"]
		println "puppet-service.groovy.install: domainName is ${domainName}"
        bootstrap = PuppetBootstrap.getBootstrap(context:context, server:puppetMasterIp, environment:puppetEnvironment,
      puppetNodePrefix:puppetNodePrefix, domainName:domainName)
		println "puppet-service.groovy.install: Running bootstrap.install() ..."
        bootstrap.install()
/*
        if (binding.variables["puppetMode"] && !puppetApplicationName.is(null) && !puppetMasterServiceName.is(null)) {
            puppetMasterPU = waitForGlobalService(puppetApplicationName, puppetMasterServiceName, 600, TimeUnit.SECONDS)
            puppetMasterPU.invoke("puppetCertSign", [context.hostName])
        }
*/
      }
      start {
        puppetEnvironment = context.attributes.application["puppet_environment"] ?: binding.variables.get("puppetEnvironment") ?: context.applicationName
		println "puppet-service.groovy.start: puppetEnvironment is ${puppetEnvironment}"
        puppetMasterIp = context.attributes.global["puppet_master_ip"] ?: binding.variables.get("puppetMasterIp")
		println "puppet-service.groovy.start: puppetMasterIp is ${puppetMasterIp}"
        puppetNodePrefix = binding.variables.get("puppetNodePrefix") ?: context.attributes.global["puppetNodePrefix"]
		println "puppet-service.groovy.start: puppetNodePrefix is ${puppetNodePrefix}"
        domainName = binding.variables.get("domainName") ?: context.attributes.global["domainName"]
		println "puppet-service.groovy.start: domainName is ${domainName}"
        bootstrap = PuppetBootstrap.getBootstrap(context:context, server:puppetMasterIp, environment:puppetEnvironment, 
      puppetNodePrefix:puppetNodePrefix, domainName:domainName)
		println "puppet-service.groovy.start: Running bootstrap.start() ..."
        bootstrap.configure() // ensure server address is current

        if (binding.variables["puppetMode"] == "agent" && !puppetMasterIp.is(null)) {
            tags = binding.variables.get("puppetTags") ?: []
            println "puppet-service.groovy.start: Running puppet agent with server ${puppetMasterIp} and environment ${puppetEnvironment} HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH"
			println "puppet-service.groovy.start: Tags = ${tags.join(", ")}"
            bootstrap.puppetAgent(tags)
        } else {
            if (binding.variables["puppetRepo"]) {
                bootstrap.loadManifest(puppetRepo.repoType, puppetRepo.repoUrl)
                if (puppetRepo.manifestPath) {
                    bootstrap.applyManifest(puppetRepo.manifestPath)
                } else if (puppetRepo.classes) {
                    bootstrap.applyClasses(puppetRepo.classes)
                } else {
                    println "puppet-service.groovy.start: Puppet repository loaded but nothing was applied."
                }
            } else {
                println "puppet-service.groovy.start: Puppet repository is undefined in the properties file."
            }
        }
      }
      return true // refrain from returning a number, GSC might think it's a pid
    }

    customCommands([
        "load_manifest": {repoType, repoUrl ->
            PuppetBootstrap.getBootstrap(context:context).loadManifest(repoType, repoUrl)
        },
        "apply_manifest": {manifestPath, manifestSource="repo" ->
            PuppetBootstrap.getBootstrap(context:context).applyManifest(manifestPath, manifestSource)
        },
        "apply_classes": {classesJson ->
            Map classes = new JsonSlurper().parseText(classesJson)
            PuppetBootstrap.getBootstrap(context:context).applyClasses(classes)
        },
        "cleanup_repo": { 
            bootstrap = PuppetBootstrap.getBootstrap(context:context)
            bootstrap.cleanup_local_repo()
        }
    ])   
}
