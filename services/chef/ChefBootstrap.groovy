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

class ChefBootstrap {
    protected Map chefConfig
    protected def osConfig
    protected def os
    protected String chefBinPath
    protected ServiceContext context = null
    def opscode_gpg_key_url = "http://apt.opscode.com/packages@opscode.com.gpg.key"

    public def static getBootstrap(options=[:]) {
        def os = OperatingSystem.getInstance()
        def cls
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

    protected def ChefBootstrap(options=[:]) {
        os = OperatingSystem.getInstance()
        if ("context" in options) {
            context = options["context"]
        }
        if (context.is(null)) {
            context = ServiceContextFactory.getServiceContext()
        }
        def chefProperties = new ConfigSlurper().parse(new File(pathJoin(context.getServiceDirectory(), "chef-service.properties")).toURL())
        osConfig = os.isWin32() ? chefProperties.win32 : chefProperties.unix

        // Load chef config from context attributes
        chefConfig = context.attributes.thisInstance.containsKey("chefConfig") ? context.attributes.thisInstance.get("chefConfig") : [:]
        // merge configs: defaults from properties file, persisted config from attributes, options given to this function
        chefConfig = chefProperties.chef.flatten() + chefConfig + options.findAll(){ it.key != "context" }
        // persist to context attributes
        context.attributes.thisInstance["chefConfig"] = chefConfig
    }
    protected def installRuby() {
        if (this.class.methods.find { it.name == "install_pkgs"}) {
            install_pkgs(rubyPkgs)
        } else {
            rvm()
        }
    }
    protected def installRubyGems() {
        if (!which("gem").isEmpty()) { return }
        //install rubygems from source to avoid a version mismatch in rubygems (see rubygems.org)
        def tmpDir = getTmpDir()
        def gemTarball = pathJoin(tmpDir, "rubygems.tar.gz")
        def gemDir = pathJoin(tmpDir, "gemInstall")
        new File(gemDir).mkdir()
        download(gemTarball, chefConfig.gemTarballUrl)
        sh("tar -xzf ${gemTarball} --strip-components=1 -C ${gemDir}")
        sudo("ruby ${gemDir}/setup.rb --no-format-executable")
    }
    public def install() {
        if (which("chef-solo").isEmpty()) {
            switch(chefConfig.installFlavor) {
                case ["fatBinary", "pkg"]: break
                case "gem":
                    installRuby() //there are multiple packages here, and we want to be sure they're all installed
                    installRubyGems()
                    break
                default:
                    throw new Exception("Support for the install flavor ${chefConfig.installFlavor} is not implemented")
                    break
            }
            this.invokeMethod("${chefConfig.installFlavor}Install", null)
        }
    }
    protected def gemInstall() {
        def opts = "-y --no-rdoc --no-ri"
        if (!chefConfig.version.is(null)) {
            opts = "-v ${chefConfig.version} " + opts
        }
        sudo("gem install chef ${opts}")
    }
    protected def mkChefDirs() {
        sudo("mkdir -p '/etc/chef' '/var/chef' '/var/log/chef'")
    }
    protected def configureClient() {
		if ("serverURL" in chefConfig) {
			if (chefConfig.serverURL == null) {
				throw new RuntimeException("Cannot find a chef server URL in thisApplication nor in global attribute 'chef_server_url', chefConfig.serverURL is null")
			}
		}
		else {
			throw new RuntimeException("Cannot find a chef server URL (serverURL) in chefConfig")
		}


        mkChefDirs()
		def environment = chefConfig.environment ?: "_default"
		def validationClientName = chefConfig.validationClientName ?: "chef-validator"
        sudoWriteFile("/etc/chef/client.rb", """
log_level          :info
log_location       "/var/log/chef/client.log"
ssl_verify_mode    :verify_none
validation_client_name "${validationClientName}"
validation_key         "/etc/chef/validation.pem"
client_key             "/etc/chef/client.pem"
chef_server_url    "${chefConfig.serverURL}"
environment    "${environment}"
file_cache_path    "/var/chef/cache"
file_backup_path   "/var/chef/backup"
pid_file           "/var/run/chef/client.pid"
Chef::Log::Formatter.show_time = true
""")
        if (chefConfig.validationCert) {
            sudoWriteFile("/etc/chef/validation.pem", chefConfig.validationCert)
        } else if (test("stat ${System.properties["user.home"]}/gs-files/validation.pem")) {
            //a validation file was put in the service directory
            sudo("cp -f ${System.properties["user.home"]}/gs-files/validation.pem /etc/chef/validation.pem")
        } else {
            throw new Exception("Cannot find a chef validation certificate for this client. Please provide the certificate either in the validationCert property or as a 'validation.pem' file in the service directory")
        }

		if (chefConfig.encryptedDataBagSecret) {
            sudoWriteFile("/etc/chef/encrypted_data_bag_secret", chefConfig.encryptedDataBagSecret)
        }
    }
    public def runClient(ArrayList runList) {
        runClient(runListToInitialJson(runList))
    }
    public def runClient(HashMap initJson=[:]) {
        configureClient()
        initJson["cloudify"] = context.attributes.thisService["chef"]
        def jsonFile = new File(pathJoin(context.getServiceDirectory(), "chef_client.json"))
        jsonFile.withWriter() { it.write(JsonOutput.toJson(initJson)) }
        sudo("chef-client -j ${jsonFile.getPath()}")
    }
    public def runApply(String inlineRecipe) {
        if (chefConfig.version.tokenize('.')[0].toInteger() < 11 ) {
            throw new Exception("chef-apply is only available on Chef 11")
        }
        // It's safer to save the string to file instead of meddling with quoting issues
        def tempRecipeFile = File.createTempFile("inlineChefRecipe-${context.getServiceName()}", ".rb")
        tempRecipeFile.write(inlineRecipe)
        def chef_apply = which("chef-apply")
        print "which(chef-apply)=${chef_apply}"
        assert !chef_apply.isEmpty()
        sudo("${chef_apply} ${tempRecipeFile.getAbsolutePath()}")
        tempRecipeFile.delete()
    }
    public def runSolo(ArrayList runList, String cookbooksUrl=null, String cookbooksPath=null) {
        runSolo(runListToInitialJson(runList), cookbooksUrl, cookbooksPath)
    }
    public def runSolo(HashMap initJson=[:], String cookbooksUrl=null, String cookbooksPath=null) {
        File soloTmpDir = getTmpDir() as File
        assert(!soloTmpDir.is(null))
        if (!cookbooksUrl.is(null) && isURL(cookbooksUrl)) {
        } else if ("bootstrapCookbooksUrl" in chefConfig && isURL(chefConfig.bootstrapCookbooksUrl)) {
            execSolo(initJson, soloTmpDir, cookbooksUrl)
        } else if (!cookbooksPath.is(null)) {
            println "Running chef-solo with cookbooksPath: ${cookbooksPath}"
            execSolo(initJson, soloTmpDir, null, cookbooksPath)
        } else if (cookbooksPath.is(null) && berksfileExists()) {
            def berkshelfCookbooksPath = new File(soloTmpDir, "cookbooks")
            getCookbooksWithBerkshelf(berkshelfCookbooksPath)
            println "Running chef-solo with berkshelf cookbooks"
            execSolo(initJson, soloTmpDir, null, berkshelfCookbooksPath.path)
        } else {
            throw new Exception("No Berksfile present and cookbooksUrl, cookbooksPath are not set")
        }
        soloTmpDir.deleteDir()
    }
    protected execSolo(HashMap initJson=[:], File soloTmpDir, String cookbooksUrl=null, String cookbooksPath=null) {
        def chef_solo = which("chef-solo")
        assert ! chef_solo.isEmpty()
        def soloConf = new File(soloTmpDir, "solo.rb") << """
file_cache_path "${soloTmpDir}"
cookbook_path "${cookbooksPath}"
"""
        def jsonFile = new File(soloTmpDir, "dna.json") << JsonOutput.toJson(initJson)
        if (cookbooksUrl.is(null)) {
            sudo("""${chef_solo} -c ${soloConf} -j ${jsonFile}""")
        } else {
            sudo("""${chef_solo} -c ${soloConf} -j ${jsonFile} -r ${chefConfig.bootstrapCookbooksUrl}""")
        }
    }
    protected runListToInitialJson(ArrayList runList) {
        def initJson = [:]
        if (!runList.isEmpty()) {
            initJson["run_list"] = runList
        }
        return initJson
    }
    protected def fatBinaryInstall() {
        chefBinPath = "/opt/chef/bin"
        new AntBuilder().sequential {
            mkdir(dir:osConfig.installDir)
            get(src:osConfig.scriptUrl, dest:"${osConfig.installDir}/${osConfig.installer}", skipexisting:true)
            chmod(osfamily:"unix", perm:"0755", file:"${osConfig.installDir}/${osConfig.installer}")
            exec(osfamily:"windows", executable:"msiexec") {
                ["/i", "/q", "${osConfig.installDir}/${osConfig.installer}"].each { arg(value:it) }
            }
        }
        if (os.getVendor() != "Win32") {
            def versionParam = chefConfig.version ? "-v ${chefConfig.version}" : ""
            sudo("${osConfig.installDir}/${osConfig.installer} ${versionParam}")
        }
    }
    protected berksfileExists() {
        return new File(context.getServiceDirectory(), "Berksfile").exists()
    }
    protected def rvm() {
        // not implemented yet
        println "RVM install method is not implemented yet"
    }
    protected def which(binary) {
        // check for binaries we already know about
        def filename
        if (binary.startsWith("chef-")) {
            filename = pathJoin(getChefBinPath(), binary)
            if (new File(filename).exists()) {
                return filename
            } else {
                return ""
            }
        } else {
            return shellOut("which $binary")
        }
    }
    protected isURL(String urlString) {
        try {
            urlString.toURL()
            return true
        } catch (java.net.MalformedURLException e) {
            return false
        } catch (java.lang.NullPointerException e) {
            return false
        }
    }
    protected String getGemsBinPath() {
        def ret = shellOut("gem env")
        if (ret.is(null)) {
            return null
        } else {
            ret.split("\n").find { it =~ "EXECUTABLE DIRECTORY" }.split(":")[1].stripIndent()
        }
    }
    protected def getChefBinPath() {
        def path
        switch (chefConfig.installFlavor) {
            case "gem":
                if (! which("gem").isEmpty()) {
                    path = getGemsBinPath()
                } else { path = "" }
                break
            case "fatBinary":
                path = "/opt/chef/bin"
                break
            default:
                path = binPath
        }
        return path
    }
    protected def getChefGemBinPath() {
        def path
        switch (chefConfig.installFlavor) {
            case "gem":
                if (! which("gem").isEmpty()) {
                    path = getGemsBinPath()
                } else { path = "" }
                break
            case "fatBinary":
                path = "/opt/chef/embedded/bin"
                break
            default:
                path = binPath
        }
        return path
    }
    public def getConfig() {
        return chefConfig
    }
    protected def getCookbooksWithBerkshelf(cookbooksPath) {
        // First, make sure we have berkshelf
        runSolo(["recipe[berkshelf]"], null, pathJoin(context.getServiceDirectory(), "berks-cookbooks"))
        // Now, run it to retrieve all other cookbooks
        def gemBinPath = getChefGemBinPath()
        println "Getting cookbooks with berkshelf"
        sh(["${gemBinPath}/berks", "install", "--path", "${cookbooksPath}"], cwd:context.getServiceDirectory())
    }
    protected gemInstallGem(gem) {
        // TODO: adapt for rvm
        sudo("gem install ${gem} --no-ri --no-rdoc")
    }
}

class DebianBootstrap extends ChefBootstrap {
    def DebianBootstrap(options) { super(options) }

    def rubyPkgs = ["ruby-dev", "ruby", "ruby-json", "libopenssl-ruby", "build-essential"]
    def binPath = "/usr/bin"
    def install_pkgs(List pkgs) {
        sudo("apt-get update")
        sudo("apt-get install -y ${pkgs.join(" ")}", [env: ["DEBIAN_FRONTEND": "noninteractive", "DEBIAN_PRIORITY": "critical"]])
    }
    def pkgInstall() {
        //TODO: when opscode notice that they're not in 0.10 anymore, we should change this accordingly through properties
        sudoWriteFile("/etc/apt/sources.list.d/opscode.list", """
deb http://apt.opscode.com/ ${os.getVendorCodeName().toLowerCase()}-0.10 main
""")
        sudo("wget -O - ${opscode_gpg_key_url} | apt-key add -")
        sudo("apt-get update")
        sudo("""echo "chef chef/chef_server_url string ${chefConfig.serverURL}" | sudo debconf-set-selections""")
        install_pkgs(["opscode-keyring", "chef"])
    }
}

class RHELBootstrap extends ChefBootstrap {
    def RHELBootstrap(options) { super(options) }
    def rubyPkgs = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]
    def binPath = "/usr/bin"
    def install(options) {
        if (os.getVendor() in ["CentOS", "Red Hat"]) {
            def shortVersion = os.getVendorVersion().tokenize(".")[0]
            if (shortVersion.toInteger() < 6) {
                sudo("wget -O /etc/yum.repos.d/aegisco.repo http://rpm.aegisco.com/aegisco/el5/aegisco.repo")
            }
            sudo("rpm -Uvh http://rbel.frameos.org/rbel${shortVersion}")
        }
        return super.install(options)
    }
    def install_pkgs(List pkgs) {
        sudo("yum install -y ${pkgs.join(" ")}")
    }
    def gemInstall() {
        sudo("gem update --system")
        super.gemInstall()
    }
}

class SuSEBootstrap extends ChefBootstrap {
    def SuSEBootstrap(options) { super(options) }
    def rubyPkgs = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]
    def binPath = "/usr/bin"
    def install_pkgs(List pkgs) {
        sudo("zypper install ${pkgs.join(" ")}")
    }
}

class WindowsBootstrap extends ChefBootstrap {
    def WindowsBootstrap(options) { super(options) }
}
