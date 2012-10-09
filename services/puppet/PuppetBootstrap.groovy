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

import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext
import groovy.json.JsonOutput
import static Shell.*

class puppetBootstrap {
    // factory method for getting the appropriate bootstrap class
    def static getBootstrap(options=[:]) {
        def os = OperatingSystem.getInstance()
        def cls
        def puppetPackages = ["puppet"]
        String local_repo_dir = underHomeDir("cloudify-recipes")
        switch (os.getVendor()) {
            case ["Ubuntu", "Debian", "Mint"]: cls = new DebianBootstrap(options); break
            case ["Red Hat", "CentOS", "Fedora", "Amazon"]: cls = new RHELBootstrap(options); break
            case "SuSE":  cls = new SuSEBootstrap(options); break
            case "Win32": cls = new WindowsBootstrap(options); break
            case "" /*returned by ec2linux*/:
                if (test("grep 'Amazon Linux' /etc/issue")) {
                    cls = new RHELBootstrap(options); break
                }
            default: throw new Exception("Support for the OS ${os.getVendor()} is not implemented")
        }
        return cls
    }
    
    def PuppetBootstrap(options=[:]) { 
        os = OperatingSystem.getInstance()
        if ("context" in options) {
            context = options["context"]
        }
        if (context.is(null)) {
            context = ServiceContextFactory.getServiceContext()
        }
        def puppetProperties = new ConfigSlurper().parse(new File(pathJoin(context.getServiceDirectory(), "puppet.properties")).toURL())
        osConfig = os.isWin32() ? puppetProperties.win32 : puppetProperties.unix

        // Load puppet config from context attributes
        puppetConfig = context.attributes.thisInstance.containsKey("puppetConfig") ? context.attributes.thisInstance.get("puppetConfig") : [:]
        // merge configs: defaults from properties file, persisted config from attributes, options given to this function
        puppetConfig = puppetProperties.puppet.flatten() + puppetConfig + options.findAll(){ it.key != "context" }
        // persist to context attributes
        context.attributes.thisInstance["puppetConfig"] = puppetConfig
    }

    def install(options) { 
        puppetConfTemplate = new File("templates", "puppet.conf").getText()
        def binding = [environment: puppetConfig.environment, server: puppetConfig.server]
        sudoWriteFile("/etc/puppet/puppet.conf", SimpleTemplateEngine(puppetConfTemplate).make(binding).toString())     
    }

    def loadManifest(manifestOriginType, manifestOriginUrl) {
        /*stuff like in ChefLoader -- TODO Refactoring into groovy-utils*/
        if (manifestOriginType == "tar") {
            String local_tarball_path = underHomeDir("manifests.tgz")
            download(local_tarball_path, manifestOriginUrl)
            sh("tar -xzf ${local_tarball_path} -C ${local_repo_dir}")
        }
    }

    def appplyManifest() {
        manifest = pathJoin(local_repo_dir, "manifests/site.pp") //TODO: move to parameter
        sudo("puppet apply ${manifest}")
    }

}

class DebianBootstrap extends puppetBootstrap {
    def DebianBootstrap(options) { super(options) }
    def install(options) {
        installPuppetlabsRepo()
        installPkgs(puppetPkgs)
        return super.install(options)
    }

    def installPuppetlabsRepo() {
        def versionName = os.getVendorCodeName().toLowerCase()
        dpkg(puppetConfig.puppetlabsRepoDpkg."${versionName}")
    }
    def dpkg(deb_url) { 
        local_deb = underHomeDir("deb")
        download(local_deb, deb_url)
        sudo("dpkg -i ${local_deb}")    
    }
    def installPkgs(List pkgs) {
        sudo("apt-get install -y ${pkgs.join(" ")}")
    }
}

class RHELBootstrap extends puppetBootstrap {
    def RHELBootstrap(options) { super(options) }
    def install(options) {
        installPuppetlabsRepo()
        installPkgs(puppetPkgs)
        return super.install(options)
    }

    def installPuppetlabsRepo() {
        def shortVersion = os.getVendorVersion().tokenize(".")[0]
        rpm(puppetConfig.puppetlabsRepoRpm."rhel${shortVersion}")
    }
    def rpm(repo) { 
        sudo("rpm -ivh ${repo}")    
    }
    def installPkgs(List pkgs) {
        sudo("yum install -y ${pkgs.join(" ")}")
    }
    def gemInstall() {  //not currently used
        sudo("gem update --system")
        super.gemInstall()
    }
    
}

class WindowsBootstrap extends puppetBootstrap {
}