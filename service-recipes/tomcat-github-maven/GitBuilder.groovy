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
		config = new ConfigSlurper().parse(new File("git.properties").toURL())
		context = ServiceContextFactory.getServiceContext()
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
     def privateKeyFilename="id_rsa"	 
	 if (!new File("${context.serviceDirectory}/.ssh/${privateKeyFilename}").exists()) {
	   throw new java.io.FileNotFoundException("place add a private key to the recipe for accessing github: ${privateKeySourceFolder}/${privateKeyFilename}")
	 }
	}
	
	void installGit() {
	 
	 if (ServiceUtils.isWindows()) {
	   //windows uses git portable which is configured to use ssh keys directly from recipe folder
	   validateSshKeysInRecipe()
	 }
	 else if (!context.isLocalCloud()) {
	  // install ssh keys on linux cloud machines
	  validateSshKeysInRecipe()
	  def privateKeyTargetFolder="${System.properties["user.home"]}/.ssh"
	  ant.echo("installing ssh keys")
      ant.mkdir(dir:privateKeyTargetFolder)
      ant.copy(todir: privateKeyTargetFolder, overwrite:true) {
       fileset(dir: "${context.serviceDirectory}/.ssh")
      }
	  ant.echo("modifying private ssh key file permissions")
	  ant.chmod(dir: privateKeyTargetFolder, perm:"600", includes:"**/*")
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