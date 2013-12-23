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
*******************************************************************************/

import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.domain.context.ServiceContext
import groovy.json.JsonOutput
import static Shell.*

class PuppetBootstrap {
    Map puppetConfig
    def osConfig
    def os
    ServiceContext context = null
    def extraPuppetPackages = []
    String local_repo_dir = underHomeDir("cloudify/puppet")
    String local_custom_facts = "/opt/cloudify/puppet/facts"
    String cloudify_module_dir = "/opt/cloudify/puppet/modules/cloudify"
    String metadata_file = "/opt/cloudify/metadata.json"

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
		println "In PuppetBootstrap.groovy.install"
        sh("mkdir -p '${local_repo_dir}'" )
        //import facter plugin
        sudo("mkdir -p ${local_custom_facts} ${cloudify_module_dir}")
        def custom_facts_dir = pathJoin(context.getServiceDirectory(),"custom_facts")
        sudo("cp -r '${custom_facts_dir}'/* '${local_custom_facts}'")
        def additional_lib_dir = pathJoin(context.getServiceDirectory(),"lib")
        sudo("cp -r '${additional_lib_dir}' '${cloudify_module_dir}'/")

        //write down the management machine and instance metadata for use by puppet modules
        def metadata = [:]
        metadata["managementIP"] = System.getenv("LOOKUPLOCATORS").split(":")[0]
        metadata["REST_port"] = 8100
        metadata["application"] = context.getApplicationName()
        metadata["service"] = context.getServiceName()
        metadata["instanceID"] = context.getInstanceId()
        def tmp_file = File.createTempFile("metadata", "json")
        tmp_file.withWriter() { it.write(JsonOutput.toJson(metadata)) }
        sudo("mv '${tmp_file}' '${metadata_file}'")
		println "End of PuppetBootstrap.groovy.install"
        configure()
    }

    def configure() {
        def templatesDir = pathJoin(context.getServiceDirectory(),"templates")
        def templateEngine = new groovy.text.SimpleTemplateEngine()
        def puppetConfTemplate = new File(templatesDir, "puppet.conf").getText()
        def puppetConf = templateEngine.createTemplate(puppetConfTemplate).make(
                            [environment: sanitizeEnvironment(puppetConfig.environment),
                            server: puppetConfig.server,
                            //nodeName: "pcha-" + context.getServiceName() + context.instanceId + ".pcha-prod.cloud.dc4.local",
                            nodeName: puppetConfig.puppetNodePrefix + "-" + context.getServiceName() + 
                    //context.instanceId + "." + System.getenv("TENANT") + puppetConfig.domainName,
                    context.instanceId + ".pcha-prod" + "." + puppetConfig.domainName,
                            cloudify_module_path:pathJoin(local_repo_dir, "modules")]
                        )
        sudoWriteFile("/etc/puppet/puppet.conf", puppetConf.toString())
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
                sh("git clone --recursive ${repoUrl} ${local_repo_dir}")
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
                sh("svn co '${repoUrl}' '${local_repo_dir}'")
            }
            break
        case "tar":
            String local_tarball_path = underHomeDir("manifests.tgz")
            download(local_tarball_path, repoUrl)
            sh("tar -xzf '${local_tarball_path}' -C '${local_repo_dir}'")
            break
        default:
            throw new Exception("Unrecognized type '${repoType}', please use one of: 'git', 'svn' or 'tar'")
        }
    }

    def applyManifest(manifestPath="manifests/site.pp", manifestSource="repo") {
        String manifest
        switch (manifestSource) {
        case "repo":
            manifest = pathJoin(local_repo_dir, manifestPath)
            break
        case "service":
            manifest = pathJoin(context.getServiceDirectory(), manifestPath)
            break
        default:
            throw new Exception("Unrecognized manifest source '${manifestSource}', please use either 'repo' or 'service'")
        }
        puppetApply(manifest)
    }

    def toPuppet(ArrayList expr) {
        "[" + expr.collect() { i -> toPuppet(i) }.join(",\n") + "]"
    }
    def toPuppet(Map expr) {
        "{\n" + expr.collect() { k, v -> "${k} => ${toPuppet(v)}"}.join(",\n") + "}"
    }
    def toPuppet(expr) {
        "\"${expr}\""
    }

    // Apply puppet classes. classes argument is a map of classes -> parameters
    // classes must be available within modules in the modulepath
    def applyClasses(Map classes) {
        puppetExecute(
            classes.collect() { kls, params ->
                "class{'${kls}':\n" +
                toPuppet(params)[1..-1]// slice off the first curly cause it isn't really a hash
            }.join("\n")
        )
    }

    def puppetAgent(tags=[]) { 
		println "tags is null " + (tags==null)
        def tagArgs = tags.any() ? ["--tags", tags.join(",")] : []
		println "After join XXXXXXXXXXXXXX"
        def args = ["puppet", "agent",
            "--onetime", "--no-daemonize",
            "--logdest", "console",
            "--logdest", "syslog"]
        args += tagArgs
		println "Inside puppetAgent before the sudo args = ${args}"
        sudo(args)
		println "Finish sudo in puppetAgent "
    }

    // Execute arbitrary puppet code
    def puppetExecute(String puppetCode) {
        File tmp_file = File.createTempFile("apply_manifest", ".pp")
        tmp_file.withWriter { it.write(puppetCode)}
        puppetApply(tmp_file)
    }

    // Apply a puppet manifest
    def puppetApply(filepath) {
        sudo("puppet apply ${filepath}")
    }

    def cleanup_local_repo() {
        sh("rm -rf '${pathJoin(local_repo_dir,"*")}'")
    }

    def sanitizeEnvironment(environment) { 
        def environ = environment.tr("- .", "_")    
        if (!(environment =~ /[A-Za-z0-9_]+/).matches()) {
            throw new Exception("puppet environment must contain only alphanumeric characters or underscores, you gave \"${environment}\"")
        }
        return environ
    }
}

class DebianBootstrap extends PuppetBootstrap {
    def DebianBootstrap(options) { super(options) }

    def install(options) {
        installPuppetlabsRepo()
        installPkg("puppet", puppetConfig.version)
        if (extraPuppetPackages.any()) { installPkgs(extraPuppetPackages) }
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

    def installPkg(String pkg, version=null) {
        if (version.is(null) || version.isEmpty()) {
            sudo("apt-get install -y ${pkg}")
        } else {
            sudo("apt-get install -y ${pkg}=${version}")
        }
    }

    def installPkgs(List pkgs) {
        sudo("apt-get install -y ${pkgs.join(" ")}")
    }
}

class RHELBootstrap extends PuppetBootstrap {
    def extraPuppetPackages = super.extraPuppetPackages + ["rubygem-json"]

    def RHELBootstrap(options) { super(options) }

    def install(options) {
        installPuppetlabsRepo()
        installPkg("puppet", puppetConfig.version)
        installPkgs(extraPuppetPackages)
        return super.install(options)
    }

    def installPuppetlabsRepo() {
        def shortVersion = os.getVendorVersion().tokenize(".")[0]
        rpm(puppetConfig.puppetlabsRepoRpm."rhel${shortVersion}")
    }

    def rpm(repo) { 
        sudo("rpm -ivh ${repo}")    
    }

    def installPkg(String pkg, version=null) {
        if (version.is(null) || version.isEmpty()) {
            sudo("yum install -y ${pkg}")
        } else {
            sudo("yum install -y ${pkg}-${version}")
        }
    }

    def installPkgs(List pkgs) {
        sudo("yum install -y ${pkgs.join(" ")}")
    }
}

class AmazonBootstrap extends PuppetBootstrap {
    def extraPuppetPackages = super.extraPuppetPackages + ["rubygem-json"]

    def AmazonBootstrap(options) { super(options) }

    def install(options) {
        installPkg("puppet", puppetConfig.version)
        installPkgs(extraPuppetPackages)
        return super.install(options)
    }

    def installPkg(String pkg, version=null) {
        if (version.is(null) || version.isEmpty()) {
            sudo("yum install -y ${pkg}")
        } else {
            sudo("yum install -y ${pkg}-${version}")
        }
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
        if (extraPuppetPackages.any()) { installPkgs(extraPuppetPackages) }
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
