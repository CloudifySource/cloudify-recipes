/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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

import static Shell.*

class ChefLoader{ 
    def static get_loader(type="git") {
           switch (type) {
            case "git":
                return new ChefGitLoader()
                //break unneeded
            case "svn":
                return new ChefSvnLoader()
                //break unneeded
            case "tgz":
                return new ChefTgzLoader()
                //break unneeded
            default:
              throw new Exception("Unrecognized type(${type}), please use one of: 'git', 'svn' or 'tgz'")
            }
    }
}

abstract class ChefLoaderBase {
    String local_repo_dir = underHomeDir("cloudify-recipes")

    //TODO: refactor - this is already better implemented in ChefBootstrap, but not available from here
    def install_pkg(pkg) {
        if (test("which apt-get >/dev/null")) {
            sudo("apt-get install -y ${pkg}",
                 [env: ["DEBIAN_FRONTEND": "noninteractive", "DEBIAN_PRIORITY": "critical"]])
        } else if (test("which yum >/dev/null")) {
            sudo("yum install -y ${pkg}")
        } else if (test("which zypper >/dev/null")) {
            sudo("zypper install ${pkg}")
        } else {
            throw new Exception("Failed to find a package manager")
        }        
    }

    def initialize() {
        if (! test("ruby -r mime/types -e true")) {
            install_pkg("ruby-mime-types")
        }

        sh("mkdir -p ${local_repo_dir} ${underHomeDir(".chef")}")

        def webui_pem = underHomeDir(".chef/chef-webui.pem")
        sudoWriteFile(underHomeDir(".chef/knife.rb"), 
"""
log_level :info
log_location STDOUT
node_name 'chef-webui'
client_key '${webui_pem}'
validation_client_name 'chef-validator'
validation_key '${underHomeDir(".chef/chef-validator.pem")}'
chef_server_url 'http://localhost:4000'
cache_type 'BasicFile'
cache_options( :path => '${underHomeDir(".chef/checksums")}' )
cookbook_path [ '${underHomeDir("cookbooks")}' ]
""")

        sudo("cp -f /etc/chef/webui.pem ${webui_pem}")
        sudo("chown `whoami` ${webui_pem}")
    }

    abstract fetch(url, inner_path)

    def symlink(inner_path) {
        if (inner_path==null) inner_path = ""
        ["cookbooks", "roles", "environments"].each{ chef_dir ->
            def chef_dir_in_repo = pathJoin(local_repo_dir, inner_path, chef_dir)
            sh("rm -f ${underHomeDir(chef_dir)}")
            sh("ln -sf ${chef_dir_in_repo} ${underHomeDir(chef_dir)}") 
        }
    }

    def upload() {
        sudo("knife cookbook upload -a")
        def roles_dir = underHomeDir("roles")
        if (pathExists(roles_dir)) {
            new File(roles_dir).eachFile{file->
                sudo("knife role from file ${file.getAbsolutePath()}")
            }
        }
    }

    def listCookbooks() {
        sudoShellOut("knife cookbook list")
    }

    def invokeKnife(args = []) {
        sudoShellOut("knife " + args.join(" "))
    }

    def cleanup_local_repo() {
        sh("rm -rf ${pathJoin(local_repo_dir,"*")}")
    }
}

class ChefGitLoader extends ChefLoaderBase {
    def fetch(url, inner_path) {
        if (! test("which git >/dev/null")) {
            install_pkg("git")
        }

        def git_dir = pathJoin(local_repo_dir, ".git")
        if (pathExists(git_dir)) {
            sh("cd ${local_repo_dir}; git pull origin master")
        } else {
            sh("git clone ${url} ${local_repo_dir}")
        }
        symlink(inner_path)
    }
}

class ChefSvnLoader extends ChefLoaderBase {
    def fetch(url, inner_path) {
        if (! test("which svn >/dev/null")) {
            install_pkg("subversion")
        }

        def svn_dir = pathJoin(local_repo_dir, ".svn")
        if (pathExists(svn_dir)) {
            sh("cd ${local_repo_dir}; svn update")
        } else {
            sh("svn co ${url} ${local_repo_dir}")
        }
        symlink(inner_path)
    }
}

class ChefTgzLoader extends ChefLoaderBase {
    String local_tarball_path = underHomeDir("chef_data.tgz")
    def fetch(url, inner_path) {
        download(local_tarball_path, url)
        sh("tar -xzf ${local_tarball_path} -C ${local_repo_dir}")
        symlink(inner_path)
    }
}
