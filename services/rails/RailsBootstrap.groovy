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
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.context.ServiceContext
import static Shell.*

class RailsBootstrap {
    Map webappConfig
    def osConfig
    def os
    ServiceContext context = null
    String webapp_dir = "/opt/webapps/rails"
    String webapp_user = "rails"

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
                    cls = new RHELBootstrap(options); break
                }
            default: throw new Exception("Support for the OS ${os.getVendor()} is not implemented")
        }
        return cls
    }
    
    def RailsBootstrap(options=[:]) { 
        os = OperatingSystem.getInstance()
        if ("context" in options) {
            context = options["context"]
        }
        if (context.is(null)) {webappConfig
            context = ServiceContextFactory.getServiceContext()
        }
        def railsProperties = new ConfigSlurper().parse(new File(pathJoin(context.getServiceDirectory(), "rails.properties")).toURL())

        // Load config from context attributes
        webappConfig = context.attributes.thisInstance["webappConfig"] ?: [:]
        // merge configs: defaults from properties file, persisted config from attributes, options given to this function
        webappConfig = railsProperties.ruby.flatten() + options['webappOpts'] + webappConfig
        webappConfig += options.findAll(){ ! ["context", "webappOpts"].contains(it.key) }
        // persist to context attributes
        context.attributes.thisInstance["webappConfig"] = webappConfig
    }

    def runHook(hook=null) {
        //Run user defined hooks in the bootstrap object's context
        if (hook) {
            hook.delegate = this
            hook()
        }
    }

    def fetchRepo() {
        /*stuff like in ChefLoader -- TODO Refactoring into groovy-utils after resolving CLOUDIFY-1147*/
        def repoType = webappConfig["repoType"]
        def repoUrl = webappConfig["repoUrl"]
        def repoTag = webappConfig["repoTag"] //Only relevant for git

        def opts = [cwd: webapp_dir, user: webapp_user]

        switch (repoType) {
        case "git":
            if (! test("which git >/dev/null")) {
                installPkgs(["git"])
            }
            def git_dir = pathJoin(webapp_dir, ".git")
            if (pathExists(git_dir)) {
                sudo("git checkout master", opts)
                sudo("git pull", opts)
                sudo("git checkout ${repoTag}", opts)
            } else {
                sudo("git clone --recursive ${repoUrl} ${webapp_dir}", opts)
                sudo("git checkout ${repoTag}", opts)
            }
            break
        case "svn":
            if (! test("which svn >/dev/null")) {
                installPkgs(["subversion"])
            }
            def svn_dir = pathJoin(webapp_dir, ".svn")
            if (pathExists(svn_dir)) {
                sudo("svn update", opts)
            } else {
                sudo("svn co '${repoUrl}' '${webapp_dir}'", opts)
            }
            break
        case "tar":
            String local_tarball_path = underHomeDir("repo.tgz")
            download(local_tarball_path, repoUrl)
            sudo("tar -xzf '${local_tarball_path}' -C '${webapp_dir}'", opts)
            break
        default:
            throw new Exception("Unrecognized type '${repoType}', please use one of: 'git', 'svn' or 'tar'")
        }
    }

    def writeTemplate(templatePath, options=webappConfig, targetPath=null) {
        targetPath = targetPath ?: pathJoin(webapp_dir, templatePath)
        String templatesDir = pathJoin(context.getServiceDirectory(),"templates")
        def templateEngine = new groovy.text.SimpleTemplateEngine()

        def template = new File(templatesDir, templatePath).getText()
        def preparedTemplate = templateEngine.createTemplate(template).make([options: options])
        sudoWriteFile(targetPath, preparedTemplate.toString())
    }

    def rubySh(command, Boolean useSudo=false, Map opts=[cwd: webapp_dir]) {
        if (useSudo)
            opts['user'] = webappConfig['rubyFlavor'] == "rvm" ? webapp_user : "root"

        //Most thorough way we found of injecting the RAILS_ENV
        command = "env RAILS_ENV=${webappConfig['rails_env']} ${command}".toString()

        if (webappConfig['rubyFlavor'] == "rvm")
            //load the rvm function
            bash("source '${underHomeDir(".rvm/scripts/rvm")}'; ${command}", opts)
        else
            sudo(command, opts)
    }

    def installRvm() {
        sh("curl -L https://get.rvm.io | bash -s stable")
        rubySh("command rvm install ${webappConfig['rubyVersion']}; \
                rvm --default use ${webappConfig['rubyVersion']}")
    }

    def install() {
        if (! test("id -u ${webapp_user}"))
            sudo("useradd ${webapp_user} -d '${webapp_dir}'")

        sudo("mkdir -m 755 -p '${webapp_dir}'")
        sudo("chown '${webapp_user}' '${webapp_dir}'")

        fetchRepo()

        rubySh("gem install bundler --no-ri --no-rdoc", true)

        ["log", "tmp", "public", "files"].each { dir ->
            sudo("mkdir -p -m 755 '${pathJoin(webapp_dir, dir)}'", [user: webapp_user])
        }
        writeTemplate("Gemfile.local")

        String unneded_envs = (["development", "test", "production"] - [webappConfig['rails_env']]).join(" ")
        rubySh("bundle install --without ${webappConfig['without_gem_groups'].join(" ")} \
                                         ${unneded_envs}", true)

        writeTemplate("config/database.yml")
    }

    def migrate() {
        //chown sqlite file to webapp user
        if (webappConfig['db']['adapter'] =="sqlite3")
            sudo("chown ${webapp_user} ${pathJoin(webapp_dir, webappConfig['db']['database'])}")

        def migration_command = webappConfig['migration_command'] ?: "bundle exec rake db:migrate"
        rubySh(migration_command, true)
    }

    def start() {
        //Set up and launch unicorn with upstart
        //TODO: make this easily configurable - move to hook?
        installPkgs(["upstart"])

        writeTemplate("unicorn.conf",
                      [webapp_dir:webapp_dir,
                       webapp_user:webapp_user,
                       rails_env:webappConfig['rails_env']
                      ],
                      "/etc/init/unicorn.conf")

        sudo("service unicorn restart")

        //TODO: add monitoring of the application through rack and incorporate into autoscaling
    }
}

class DebianBootstrap extends RailsBootstrap {
    def DebianBootstrap(options) { super(options) }
    def rubyPackage = "ruby"
    def extraRubyPackages = ["rubygems", "ruby-json", "libopenssl-ruby", "ruby-dev"]

    //TODO: make the db packages depend on properties? (e.g. rails_env?)
    def otherPackages = ["build-essential", "libxml2-dev", "libxslt-dev",
                        "libsqlite3-dev", "libmysqlclient-dev"]

    def install() {
        switch (webappConfig['rubyFlavor']) {
        case ["packages"]: 
            preparePkgs()
            installPkgVersion(webappConfig['overridePackage'] ?: rubyPackage,
                              webappConfig['rubyVersion'])
            installPkgs(extraRubyPackages + otherPackages)
            break
        case ["rvm"]:
            preparePkgs()
            installPkgs(otherPackages)
            installRvm()
            break
        default:
            throw new RuntimeException("Unsupported ruby flavor '${webappConfig['rubyFlavor']}'")
        }

        return super.install()
    }

    def preparePkgs() {
        sudo("apt-get update")
    }

    def installPkgs(List pkgs) {
        sudo("apt-get install -y ${pkgs.join(" ")}", [env: ["DEBIAN_FRONTEND": "noninteractive", "DEBIAN_PRIORITY": "critical"]])
    }

    def installPkgVersion(pkg, version=null) {
        if (version)
            installPkgs(["${pkg}=${version}"])
        else
            installPkgs([pkg])
    }
}

class RHELBootstrap extends RailsBootstrap {
    def RHELBootstrap(options) { super(options) }
    //TODO: add missing RHEL packages
    def rubyPackages = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]

    def install() {
        installPkgs(rubyPackages)
        return super.install()
    }

    def installPkgs(List pkgs) {
        sudo("yum install -y ${pkgs.join(" ")}")
    }
}

class SuSEBootstrap extends RailsBootstrap {
    def SuSEBootstrap(options) { super(options) }
    //TODO: add missing SuSE packages
    def rubyPackages = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]

    def install() {
        installPkgs(rubyPackages)
        return super.install()
    }

    def installPkgs(List pkgs) {
        sudo("zypper update")
        sudo("zypper install -y ${pkgs.join(" ")}")
    }
}

class WindowsBootstrap extends RailsBootstrap {
}
