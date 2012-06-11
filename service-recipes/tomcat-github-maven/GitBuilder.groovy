import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory;
import org.cloudifysource.dsl.context.ServiceContext;

class GitBuilder {
	
	String gitexec
	String installDir
	String workingDir
	ConfigObject config
	ServiceContext context
	AntBuilder ant
		
	GitBuilder() {
		ant = new AntBuilder()
		context = ServiceContextFactory.getServiceContext()
		config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/git.properties").toURL())
		
		if (ServiceUtils.isWindows()) {
		  gitexec="${context.serviceDirectory}/git/bin/git.exe"
		}
		else {
		  gitexec="${context.serviceDirectory}/git/usr/libexec/git-core/git"
		}
		
	    installDir = "${System.properties["user.home"]}/.cloudify/${context.applicationName}_${context.serviceName}_${context.instanceId}"
		setWorkingDir(context.serviceDirectory)
	}
	
	void setWorkingDir(workingDir) {
		if (!new File(workingDir).isAbsolute()) {
			this.workingDir = new File(context.serviceDirectory,workingDir).getPath()
		}
		else {
			this.workingDir = workingDir
		}
		println "git working directory changed to ${this.workingDir}"
	}
	
	void validateSshKeysInRecipe() {
	 if (!new File("${context.serviceDirectory}/.ssh/id_rsa").exists()) {
	   throw new java.io.FileNotFoundException("Private key not found. Please add it to the recipe: .ssh/id_rsa")
	 }
	 if (!new File("${context.serviceDirectory}/.ssh/known_hosts").exists()) {
	   throw new java.io.FileNotFoundException("known_hosts not found. Please add it to the recipe: .ssh/known_hosts")
	 }
	}
	
	void installGit() {
	 
	 validateSshKeysInRecipe()
	 
	 if (ServiceUtils.isWindows()) {
	   //windows uses git portable which is configured to use ssh keys directly from recipe folder
	 }
	 else { 
	   println "creating ssh.sh"
	   //use .ssh keys and known_hosts file supplied with the recipe
	   ant.chmod(dir: "${context.serviceDirectory}/.ssh", perm:"600", includes:"**/*")
	   def ssh = new File("${context.serviceDirectory}/ssh.sh")
	   ssh.withWriter { f -> 
		f.println("#!/bin/bash")
		f.println("ssh -i ${context.serviceDirectory}/.ssh/id_rsa -o UserKnownHostsFile=${context.serviceDirectory}/.ssh/known_hosts -o StrictHostKeyChecking=yes \$*")
	   }
       ant.chmod(file:ssh.path, perm:'+x')
	 }
	 	 
	 ant.mkdir(dir:installDir)
	 if (ServiceUtils.isWindows()) {
	  ant.echo("installing 7zip")
	  ant.get(src:config.sevenZADownloadUrl, dest:"${installDir}/${config.sevenZAFilename}", skipexisting:true)
	  ant.unzip(src:"${installDir}/${config.sevenZAFilename}", dest:"${context.serviceDirectory}/${config.sevenZAUnzipFolder}", overwrite:true)
	   
	  ant.echo("installing git")
	  ant.get(src:config.gitZipDownloadUrl, dest:"${installDir}/${config.gitZipFilename}", skipexisting:true)
	  ant.exec(executable:"${context.serviceDirectory}/${config.sevenZAUnzipFolder}/7za.exe", dir:"${context.serviceDirectory}", failonerror:true) {
		arg(value:"x")     // extract with directories
		arg(value:"-y")    // answer yes
		arg(value:"-ogit") //output folder git
		arg(value:"${installDir}/${config.gitZipFilename}")
	  }
	 }
	 else {
	  ant.echo("installing git")
	  ant.get(src:config.gitRpmDownloadUrl, dest:"${installDir}/${config.gitRpmFilename}", skipexisting:true)
	  ant.mkdir(dir:"${context.serviceDirectory}/git")
	  ant.exec(executable:"sh", dir:"${context.serviceDirectory}/git", failonerror:true) {
		arg(value:"-c")
		arg(value:"rpm2cpio ${installDir}/${config.gitRpmFilename} | cpio -idmv")
	  }
	 }

	 if (!(new File(gitexec).exists())) {
	  throw new FileNotFoundException(gitexec + " does not exist");
	 }
	 context.attributes.thisInstance["git"] = "${gitexec}"
	}

	private void git(gitargs) {
	    ant.echo("git ${gitargs}")
		if (ServiceUtils.isWindows()) {
			ant.exec(executable:gitexec, dir:workingDir, failonerror:true) {
			 env(key:"HOME", value: "${context.serviceDirectory}") //looks for ~/.ssh
			 env(key:"HOMEDRIVE", value: "${context.serviceDirectory}")
			 env(key:"USERPROFILE", value: "${context.serviceDirectory}")
			 for (gitarg in gitargs.split(" ")) {
			   if (gitarg) {
				arg(value:gitarg)
			   }
			 }
			}
		}
		else {
			ant.exec(executable:gitexec, dir:workingDir, failonerror:true) {
			 env(key:"GIT_SSH", value: "${context.serviceDirectory}/ssh.sh")
			 for (gitarg in gitargs.split(" ")) {
			   if (gitarg) {
				arg(value:gitarg)
			   }
			 }
			}
		}
		ant.echo("done")
	}
	
	void clone(repository,directory) {
		clone([:],repository,directory)
	}
	
	void clone(props,repository,directory) {
	    def gitflags= props.verbose?"--verbose":""
		def gitargs = "clone ${gitflags} ${repository} ${directory}"
		git(gitargs)
	}
	
	void checkout(branch) {
		checkout([:],branch)
	}
	
	void checkout(props,branch) {
		def gitargs = "checkout ${branch}"
		git(gitargs)
	}
	
	void fetch(repository) {
		fetch([:], repository)
	}
	
	void fetch(props, repository) {
		def gitargs="fetch ${repository}"
		git(gitargs)
	}
	
	void merge(commit) {
		merge([:], commit)
	}
	
	void merge(props, commit) {
		def gitargs="merge ${commit}"
		git(gitargs)
	}
	
	void branch(name,commit) {
		branch([:], name,commit)
	}
	
	void branch(props, name, commit) {
		def gitflags= props.force?"--force":""
		def gitargs="branch ${gitflags} ${name} ${commit}"
		git(gitargs)
	}
}