import org.hyperic.sigar.OperatingSystem
import static Shell.*

class ChefLoader{ 
    def static get_loader(type) {
           switch (type) {
            case "git":
                return new ChefGitLoader()
            case "svn":
                return new ChefSvnLoader()
            case "tar":
                return new ChefTarLoader()
            default:
              throw new Exception("Unrecognized type(${type}), please use one of: 'git', 'svn' or 'tar'")
            }
    }
}

abstract class ChefLoaderBase {
    String local_repo_dir = underHomeDir("cloudify-recipes")

    //TODO: refactor - this is already better implemented in ChefBootstrap, but not available from here
    def install_pkg(pkg) {
        def os_vendor = OperatingSystem.getInstance().getVendor()
        switch (os_vendor) {
            case ["Ubuntu", "Debian", "Mint"]:
                println "use apt-get"
                sudo("apt-get install -y ${pkg}",
                     [env: ["DEBIAN_FRONTEND": "noninteractive", "DEBIAN_PRIORITY": "critical"]])
                break
            case ["Red Hat", "CentOS", "Fedora", "Amazon"]:
                println "use yum"
                sudo("yum install -y ${pkg}")
                break
            default:
                throw new Exception("Support for the OS ${os_vendor} is not implemented")
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

        sudo("cp /etc/chef/webui.pem ${webui_pem}")
        sudo("chown `whoami` ${webui_pem}")
    }

    abstract fetch(url, inner_path)

    def symlink(inner_path) {
        ["cookbooks", "roles"].each{ chef_dir ->
            if (! pathExists(underHomeDir(chef_dir))) {
                def chef_dir_in_repo = pathJoin(local_repo_dir, inner_path, chef_dir)
                sh("ln -s ${chef_dir_in_repo} ${underHomeDir(chef_dir)}")
            }            
        }
    }

    def upload() {
        sudo("knife cookbook upload -a")
        def roles_dir = underHomeDir("roles")
        if (pathExists(roles_dir)) {
            sudo("knife role from file ${pathJoin(roles_dir, "*.rb")}")
        }
    }

    def cleanup() {
        sh("rm -rf ${pathJoin(local_repo_dir,"*")}")
    }
}

class ChefGitLoader extends ChefLoaderBase {
    def initialize() {
        super.initialize()
        if (! test("which git >/dev/null")) {
            install_pkg("git")
        }
    }

    def fetch(url="https://github.com/CloudifySource/cloudify-recipes.git", inner_path="apps/travel-chef") {
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
    def initialize() {
        super.initialize()
        if (! test("which svn >/dev/null")) {
            install_pkg("subversion")
        }
    }

    def fetch(url, inner_path) {
        def svn_dir = pathJoin(local_repo_dir, ".svn")
        if (pathExists(svn_dir)) {
            sh("cd ${local_repo_dir}; svn update")
        } else {
            sh("svn co ${url} ${local_repo_dir}")
        }

        symlink(inner_path)
    }
}

class ChefTarLoader extends ChefLoaderBase {
    String local_tarball_path = underHomeDir("chef_data.tgz")
    def fetch(url, inner_path) {

        //fetch the tarball (should I maybe just use wget?)
        new File(local_tarball_path).withWriter { writer ->
            writer << new URL(url).openStream()
        }

        //unpack
        sh("tar -xzf ${local_tarball_path} -C ${local_repo_dir}")

        symlink(inner_path)
    }
}