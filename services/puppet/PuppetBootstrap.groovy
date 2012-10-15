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

class PuppetBootstrap {
    Map puppetConfig
    def osConfig
    def os
    ServiceContext context = null
    def puppetPackages = ["puppet"]
    String local_repo_dir = underHomeDir("cloudify-recipes")

    // factory method for getting the appropriate bootstrap class
    def static getBootstrap(options=[:]) {
        def os = OperatingSystem.getInstance()
        def cls
        switch (os.getVendor()) {
            case ["Ubuntu", "Debian", "Mint"]: cls = new DebianBootstrap(options); break
            case ["Red Hat", "CentOS", "Fedora"]: cls = new RHELBootstrap(options); break
            case "SuSE":  cls = new SuSEBootstrap(options); break
            case "Win32": cls = new WindowsBootstrap(options); break
            case "" /*returned by ec2linux*/:
                if (test("grep 'Amazon Linux' /etc/issue")) {
                    cls = new AmazonBootstrap(options); break
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
        puppetConfig = puppetProperties.puppet + puppetConfig + options.findAll(){ it.key != "context" }
        // persist to context attributes
        context.attributes.thisInstance["puppetConfig"] = puppetConfig
    }

    def install(options) { 
        def templates_dir = pathJoin(context.getServiceDirectory(),"templates")
        def puppetConfTemplate = new File(templates_dir, "puppet.conf").getText()
        def templateEngine = new groovy.text.SimpleTemplateEngine()
        def binding = [environment: puppetConfig.environment, server: puppetConfig.server]
        sudoWriteFile("/etc/puppet/puppet.conf", 
                      templateEngine.createTemplate(puppetConfTemplate).make(binding).toString()) 

        sh("mkdir -p ${local_repo_dir}")
    }

    def loadManifest(repoType, repoUrl) {
        /*stuff like in ChefLoader -- TODO Refactoring into groovy-utils after resolving CLOUDIFY-1147*/
        switch (repoType) {
        case "git":
            if (! test("which git >/dev/null")) {
                installPkgs(["git"])
            }
            def git_dir = pathJoin(local_repo_dir, ".git")
            if (pathExists(git_dir)) {
                sh("cd ${local_repo_dir}; git pull origin master")
            } else {
                sh("git clone ${repoUrl} ${local_repo_dir}")
            }
            break
        case "svn":
            if (! test("which svn >/dev/null")) {
                installPkgs(["subversion"])
            }
            def svn_dir = pathJoin(local_repo_dir, ".svn")
            if (pathExists(svn_dir)) {
                sh("cd ${local_repo_dir}; svn update")
            } else {
                sh("svn co ${repoUrl} ${local_repo_dir}")
            }
            break
        case "tar":
            String local_tarball_path = underHomeDir("manifests.tgz")
            download(local_tarball_path, repoUrl)
            sh("tar -xzf ${local_tarball_path} -C ${local_repo_dir}")
            break
        default:
            throw new Exception("Unrecognized type '${repoType}', please use one of: 'git', 'svn' or 'tar'")
        }
    }

    def appplyManifest(manifestPath="manifests/site.pp") {
        def manifest = pathJoin(local_repo_dir, manifestPath)
        sudo("puppet apply ${manifest}")
    }

    def cleanup_local_repo() {
        sh("rm -rf ${pathJoin(local_repo_dir,"*")}")
    }
}

class DebianBootstrap extends PuppetBootstrap {
    def DebianBootstrap(options) { super(options) }

    def install(options) {
        installPuppetlabsRepo()
        installPkgs(puppetPackages)
        return super.install(options)
    }

    def installPuppetlabsRepo() {
        def versionName = os.getVendorCodeName().toLowerCase()
        dpkg(puppetConfig.puppetlabsRepoDpkg."${versionName}")
    }

    def dpkg(deb_url) { 
        def local_deb = underHomeDir("deb")
        download(local_deb, deb_url)
        sudo("dpkg -i ${local_deb}")
        sudo("apt-get update")
    }

    def installPkgs(List pkgs) {
        sudo("apt-get install -y ${pkgs.join(" ")}")
    }
}

class RHELBootstrap extends PuppetBootstrap {
    def RHELBootstrap(options) { super(options) }

    def install(options) {
        installPuppetlabsRepo()
        installPkgs(puppetPackages)
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
}

class AmazonBootstrap extends PuppetBootstrap {
    def AmazonBootstrap(options) { super(options) }

    def install(options) {
        installPkgs(puppetPackages)
        return super.install(options)
    }

    def installPkgs(List pkgs) {
        //the puppet repo is already available on amazon linux
        sudo("yum install -y ${pkgs.join(" ")}")
    }
}

class SuSEBootstrap extends PuppetBootstrap {
    def SuSEBootstrap(options) { super(options) }

    def install(options) {
        installPuppetlabsRepo()
        installPkgs(puppetPackages)
        return super.install(options)
    }

    def installPuppetlabsRepo() {
        def shortVersion = os.getVendorVersion().tokenize(".")[0]
        //TODO: possibly need to add additional repos for SuSE
        zypper(puppetConfig.puppetlabsRepoRpm."${shortVersion}")
    }

    def zypper(repo) { 
        sudo("zypper ar ${repo} puppet")    
        sudo("zypper update")
    }

    def installPkgs(List pkgs) {
        sudo("zypper install -y ${pkgs.join(" ")}")
    }
}

class WindowsBootstrap extends PuppetBootstrap {
}