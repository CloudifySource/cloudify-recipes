import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.json.JsonOutput
import shell

class ChefBootstrap {
    def chefServerURL
    def config
    def osConfig
    def os
    def chefBinPath
    def context = null
    def installFlavor
    def opscode_gpg_key_url = "http://apt.opscode.com/packages@opscode.com.gpg.key"
    def validationCert

    def static getBootstrap(options=[:]) {
        def os = OperatingSystem.getInstance()
        def cls
        switch (os.getVendor()) {
            case ["Ubuntu", "Debian", "Mint"]: cls = new DebianBootstrap(options); break
            case ["Red Hat", "CentOS", "Fedora", "Amazon"]: cls = new RHELBootstrap(options); break
            case "SuSE":  cls = new SuSEBootstrap(options); break
            case "Win32": cls = new WindowsBootstrap(options); break
            default: throw new Exception("Support for this OS is not implemented")
        }
        return cls
    }

    def ChefBootstrap(options=[:]) {
        os = OperatingSystem.getInstance()
        installFlavor = "fatBinary"
        options.each {k, v ->
            switch(k) {
                case "serverURL": chefServerURL = v; break
                case "installFlavor": installFlavor = v; break
                case "validationCert": validationCert = v; break
                case "context": context = v; break
            }
        }
        if (context.is(null)) {
            context = ServiceContextFactory.getServiceContext()
        }
        config = new ConfigSlurper().parse(new File(shell.pathJoin(context.getServiceDirectory(), "chef.properties")).toURL())
        osConfig = os.isWin32() ? config.win32 : config.unix
        chefServerURL = chefServerURL ?: config.serverURL
        validationCert = validationCert ?: config.validationCert
    }
    def install() {
        if (which("chef-solo").isEmpty()) {
            switch(installFlavor) {
                case ["fatBinary", "pkg"]: break
                default:
                    if (which("ruby").isEmpty()) {
                        if (this.class.methods.find { it.name == "install_pkgs"}) {
                            install_pkgs(rubyPkgs)
                        } else {
                            rvm()
                        }
                    }
            }
            this.invokeMethod("${installFlavor}Install", null)
        }
    }
    def gemInstall() {
        shell.sudo("gem install chef -y --no-rdoc --no-ri")
    }
    def mkChefDirs() {
        shell.sudo("mkdir -p /etc/chef")
        shell.sudo("mkdir -p /var/chef /var/log/chef")
    }
    def configureClient() {
        mkChefDirs()
        shell.sudoWriteFile("/etc/chef/client.rb", """
log_level          :info
log_location       "/var/log/chef/client.log"
ssl_verify_mode    :verify_none
validation_client_name "chef-validator"
validation_key         "/etc/chef/validation.pem"
client_key               "/etc/chef/client.pem"
chef_server_url    "${chefServerURL}"
file_cache_path    "/var/chef/cache"
file_backup_path  "/var/chef/backup"
pid_file           "/var/run/chef/client.pid"
Chef::Log::Formatter.show_time = true
""")
        if (validationCert) {
            shell.sudoWriteFile("/etc/chef/validation.pem", validationCert)
        } else {
            shell.sudo("cp ${System.properties["user.home"]}/gs-files/validation.pem /etc/chef/validation.pem")
        }
    }
    def runClient(ArrayList runList) {
        runClient(runListToInitialJson(runList))
    }
    def runClient(HashMap initJson=[:]) {
        configureClient()
        initJson["cloudify"] = context.attributes.thisService["chef"]
        def jsonFile = new File(shell.pathJoin(context.getServiceDirectory(), "chef_client.json"))
        jsonFile.withWriter() { it.write(JsonOutput.toJson(initJson)) }
        shell.sudo("chef-client -j ${jsonFile.getPath()}")
    }
    def runSolo(ArrayList runList) {
        runSolo(runListToInitialJson(runList))
    }
    def runSolo(HashMap initJson=[:]) {
        def soloConf = new File([context.getServiceDirectory(), "solo.rb"].join(File.separator)).text =
        """
file_cache_path "/tmp/chef-solo"
cookbook_path "/tmp/chef-solo/cookbooks"
        """
        def chef_solo = which("chef-solo")
        assert ! chef_solo.isEmpty()
        def jsonFile = new File(shell.pathJoin(context.getServiceDirectory(), "bootstrap_server.json"))
        jsonFile.text = JsonOutput.toJson(initJson)
        shell.sudo("""${chef_solo} -c ${context.getServiceDirectory()}/solo.rb -j ${jsonFile} -r ${config.bootstrapCookbooksUrl}""")
    }
    def runListToInitialJson(ArrayList runList) {
        def initJson = [:]
        if (!runList.isEmpty()) {
            initJson["run_list"] = runList
        }
        return initJson
    }
    def fatBinaryInstall() {
        chefBinPath = "/opt/opscode/bin"
        new AntBuilder().sequential {
            mkdir(dir:osConfig.installDir)
            get(src:osConfig.scriptUrl, dest:"${osConfig.installDir}/${osConfig.installer}", skipexisting:true)
            chmod(osfamily:"unix", perm:"0755", file:"${osConfig.installDir}/${osConfig.installer}")
            exec(osfamily:"windows", executable:"msiexec") {
                ["/i", "/q", "${osConfig.installDir}/${osConfig.installer}"].each { arg(value:it) }
            }

        }
        shell.sudo("""${osConfig.installDir}/${osConfig.installer}""")
    }
    def rvm() {
        // not implemented yet
        println "RVM install method is not implemented yet"
    }
    def which(binary) {
        // check for binaries we already know about
        def filename
        if (binary.startsWith("chef-")) {
            filename = shell.pathJoin(getChefBinPath(), binary)
            if (new File(filename).exists()) {
                return filename
            } else {
                return ""
            }
        } else {
            return shell.shellOut("which $binary")
        }
    }
    def getChefBinPath() {
        def path
        switch (installFlavor) {
            case "gem":
                if (! which("gem").isEmpty()) {
                    path = shell.shellOut("gem env").split("\n").find { it =~ "EXECUTABLE DIRECTORY" }.split(":")[1].stripIndent()
                } else { path = "" }
                break
            case "fatBinary":
                path = "/opt/opscode/bin"
                break
            default:
                path = binPath
        }
        return path
    }
}

class DebianBootstrap extends ChefBootstrap {
    def DebianBootstrap(options) { super(options) }
    def rubyPkgs = ["ruby-dev", "ruby", "ruby-json", "rubygems", "libopenssl-ruby"]
    def binPath = "/usr/bin"
    def install_pkgs(List pkgs) {
        shell.sudo("apt-get update")
        shell.sudo("apt-get install -y ${pkgs.join(" ")}", ["DEBIAN_FRONEND": "noninteractive", "DEBIAN_PRIORITY": "critical"])
    }
    def pkgInstall() {
        sudoWriteFile("/etc/apt/sources.list.d/opscode.list", """
deb http://apt.opscode.com/ ${os.getVendorCodeName().toLowerCase()}-0.10 main
""")
        sudo("wget -O - ${opscode_gpg_key_url} | apt-key add -")
        sudo("apt-get update")
        sudo("""echo "chef chef/chef_server_url string ${chefServerURL}" | sudo debconf-set-selections""")
        install_pkgs(["opscode-keyring", "chef"])
    }
}

class RHELBootstrap extends ChefBootstrap {
    def RHELBootstrap(options) { super(options) }
    def rubyPkgs = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]
    def binPath = "/usr/bin"
    def install(options) {
        if (os.getVendor() == "CentOS" || os.getVendor() == "Red Hat") {
            def shortVersion = os.getVendorVersion().tokenize(".")[0]
            if (shortVersion.toInteger() < 6) { 
                sudo("wget -O /etc/yum.repos.d/aegisco.repo http://rpm.aegisco.com/aegisco/el5/aegisco.repo")
            }
            sudo("rpm -Uvh http://rbel.frameos.org/rbel${shortVersion}")
        }
        return super.install(options)
    }
    def install_pkgs(List pkgs) {
        shell.sudo "yum install -y ${pkgs.join(" ")}"
    }
}

class SuSEBootstrap extends ChefBootstrap {
    def SuSEBootstrap(options) { super(options) }
    def rubyPkgs = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]
    def binPath = "/usr/bin"
    def install_pkgs(List pkgs) {
        shell.sudo "zypper install ${pkgs.join(" ")}"
    }
}

class WindowsBootstrap extends ChefBootstrap {
    def WindowsBootstrap(options) { super(options) }
}
