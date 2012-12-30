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
    String webapp_user = "www-data"

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
        if (context.is(null)) {
            context = ServiceContextFactory.getServiceContext()
        }
        def railsProperties = new ConfigSlurper().parse(new File(pathJoin(context.getServiceDirectory(), "rails.properties")).toURL())

        // Load puppet config from context attributes
        webappConfig = context.attributes.thisInstance.containsKey("webappConfig") ? context.attributes.thisInstance.get("webappConfig") : [:]
        // merge configs: defaults from properties file, persisted config from attributes, options given to this function
        webappConfig = railsProperties.puppet + webappConfig + options.findAll(){ it.key != "context" }
        // persist to context attributes
        context.attributes.thisInstance["webappConfig"] = webappConfig
    }

    def installApache() {
        installPkgs(["apache2"]) //add support for other distros
        //TODO: set up virtual hosts (or just use an external LB?)
    }

    def fetchRepo(options=[:]) {
        /*stuff like in ChefLoader -- TODO Refactoring into groovy-utils after resolving CLOUDIFY-1147*/
        def repoType = options["repoType"]
        def repoUrl = options["repoUrl"]
        def repoTag = options["repoTag"] //Only relevant for git

        switch (repoType) {
        case "git":
            if (! test("which git >/dev/null")) {
                installPkgs(["git"])
            }
            def git_dir = pathJoin(webapp_dir, ".git")
            if (pathExists(git_dir)) {
                sh("git pull", true, [cwd: webapp_dir])
            } else {
                sh("git clone --recursive ${repoUrl} ${webapp_dir}")
                sh("git checkout ${repoTag}", true, [cwd: webapp_dir])
            }
            break
        case "svn":
            if (! test("which svn >/dev/null")) {
                installPkgs(["subversion"])
            }
            def svn_dir = pathJoin(webapp_dir, ".svn")
            if (pathExists(svn_dir)) {
                sh("svn update", true, [cwd: webapp_dir])
            } else {
                sh("svn co '${repoUrl}' '${webapp_dir}'")
            }
            break
        case "tar":
            String local_tarball_path = underHomeDir("repo.tgz")
            download(local_tarball_path, repoUrl)
            sh("tar -xzf '${local_tarball_path}' -C '${webapp_dir}'")
            break
        default:
            throw new Exception("Unrecognized type '${repoType}', please use one of: 'git', 'svn' or 'tar'")
        }
    }

    def install(options=[:]) { 
        installApache()
        sudo("mkdir -p '${webapp_dir}'")
        sudo("chown '${webapp_user}' '${webapp_dir}'")
        sudo("chmod 777 '${webapp_dir}'")

        fetchRepo(options)

        sudo("gem install bundler")

        sudoWriteFile("${webapp_dir}/Gemfile.local", "gem 'unicorn'\n")
        sudo("bundle install --without development test rmagick postgresql", [cwd: webapp_dir])
        sudo("bundle exec rake generate_session_store", [cwd: webapp_dir])

        //TODO: point database.yml to mysql

        sudo("bundle exec rake db:migrate RAILS_ENV=production", [cwd: webapp_dir])
        sudo("bundle exec rake redmine:load_default_data RAILS_ENV=production REDMINE_LANG=en", [cwd: webapp_dir])

        //TODO: launch unicorn (or should this be done in start)
    }
}

class DebianBootstrap extends RailsBootstrap {
    def DebianBootstrap(options) { super(options) }
    def rubyPackages = ["rubygems", "ruby", "ruby-json", "libopenssl-ruby",
                        "build-essential", "ruby-dev", "libxml2-dev", "libxslt-dev",
                        "libsqlite3-dev", "libmysqlclient-dev"]

    def install(options) {
        //TODO: add rvm install option, with flavor choice including jruby
        //TODO: for both package and rvm ruby, install specified version
        installPkgs(rubyPackages)
        return super.install(options)
    }

    def installPkgs(List pkgs) {
    sudo("apt-get update")
    sudo("apt-get install -y ${pkgs.join(" ")}", [env: ["DEBIAN_FRONTEND": "noninteractive", "DEBIAN_PRIORITY": "critical"]])
    }
}

class RHELBootstrap extends RailsBootstrap {
    def RHELBootstrap(options) { super(options) }
    //TODO: add missing RHEL packages
    def rubyPackages = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]

    def install(options) {
        installPkgs(rubyPackages)
        return super.install(options)
    }

    def installPkgs(List pkgs) {
        sudo("yum install -y ${pkgs.join(" ")}")
    }
}

class SuSEBootstrap extends RailsBootstrap {
    def SuSEBootstrap(options) { super(options) }
    //TODO: add missing SuSE packages
    def rubyPackages = ["ruby", "ruby-devel", "ruby-shadow", "gcc", "gcc-c++", "automake", "autoconf", "make", "curl", "dmidecode"]

    def install(options) {
        installPkgs(rubyPackages)
        return super.install(options)
    }

    def installPkgs(List pkgs) {
        sudo("zypper update")
        sudo("zypper install -y ${pkgs.join(" ")}")
    }
}

class WindowsBootstrap extends RailsBootstrap {
}