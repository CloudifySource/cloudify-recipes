import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory;
import org.cloudifysource.dsl.context.ServiceContext;

class MavenBuilder {
		
	String mvnexec
	String installDir
	String workingDir
	ConfigObject config
	ServiceContext context
	AntBuilder ant
		
	MavenBuilder() {
		ant = new AntBuilder()
		context = ServiceContextFactory.getServiceContext()
		config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/maven.properties").toURL())
		
		if (ServiceUtils.isWindows()) {
		  mvnexec="${context.serviceDirectory}/${config.mavenUnzipFolder}/bin/mvn.bat"
		}
		else {
		  mvnexec="${context.serviceDirectory}/${config.mavenUnzipFolder}/bin/mvn"
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
		println "maven working directory changed to ${this.workingDir}"
	}
	
	void installMaven() {

     ant.echo("installing maven v${config.mavenVersion}")
	 ant.mkdir(dir:installDir)
	 ant.get(src:config.mavenDownloadUrl, dest:"${installDir}/${config.mavenZipFilename}", skipexisting:true)
	 ant.unzip(src:"${installDir}/${config.mavenZipFilename}", dest:"${context.serviceDirectory}", overwrite:true)
     if (!ServiceUtils.isWindows()) {
      ant.chmod(dir:"${context.serviceDirectory}/${config.mavenUnzipFolder}/bin", perm:'+x', excludes:"*.bat")
     }
	 if (!(new File(mvnexec).exists())) {
	  throw new FileNotFoundException(mvnexec + " does not exist");
     }
	}

	private void mvn(mvnargs) {

	 ant.echo("mvn ${mvnargs}")
	 
	 if (ServiceUtils.isWindows()) {
	  ant.exec(executable:"cmd", dir:workingDir, failonerror:true) {
		arg(value:"/c")
		arg(value:"\"${mvnexec} ${mvnargs}\"")
	   }
	 }
	 
	 else {
	  ant.exec(executable:mvnexec, dir:workingDir, failonerror:true) {
		for (mvnarg in mvnargs.split(" ")) {
		 if (mvnarg) {
			arg(value:mvnarg)
		 }
		}
	   }
	 }
	 ant.echo("done")
	}

    void cleanPackage() {
	 cleanPackage([:])
	}
	
	void cleanPackage(props) {
	 def mvnflags = props.skipTests ? "-Dmaven.test.skip=true" : ""
	 def mvnprops = "clean package ${mvnflags}"
	 mvn(mvnprops)
	}
}