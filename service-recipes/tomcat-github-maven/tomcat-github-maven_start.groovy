import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;
import java.util.concurrent.*

class CustomThreadFactory implements ThreadFactory {
    
	String name;
	
	Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
		thread.setName(name)
        return thread;
    }
}

def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def serviceName = serviceContext.getServiceName()
println "tomcat_start.groovy: This ${serviceName} instance ID is ${instanceID}"
def config=new ConfigSlurper().parse(new File("${serviceName}-service.properties").toURL())

def home= serviceContext.attributes.thisInstance["home"]

def ant = new AntBuilder()
def git = new GitBuilder(workingDir:"${serviceContext.serviceDirectory}/${config.applicationSrcFolder}")
def mvn = new MavenBuilder(workingDir:"${serviceContext.serviceDirectory}/${config.applicationSrcFolder}")

serviceContext.attributes.thisInstance["git-head"]=config.gitHead;

def update= { githead->

 //update from remote repository
 git.checkout "master"
 git.fetch "origin"
 git.merge "origin/master"
 
 //create a branch for the specified commit ${githead}
 git.branch "build", githead, force:true
 git.checkout "build"
 
 //build and deploy
 mvn.cleanPackage(skipTests:false)
 ant.echo "deploying war file"
 def outputWar="${serviceContext.serviceDirectory}/${config.applicationSrcFolder}/target/${config.applicationWarFilename}"
 ant.copy todir: "${home}/webapps", file:outputWar, overwrite:true
}

println "tomcat_start.groovy: tomcat(${instanceID}) home ${home}"

def gitHead = null
CountDownLatch latch = new CountDownLatch(1)
def executor = Executors.newSingleThreadScheduledExecutor(new CustomThreadFactory(name:"update-thread"));
executor.scheduleWithFixedDelay({
    try {
    //update git if head configuration changed
	def currentGitHead = serviceContext.attributes.thisInstance["git-head"]
	if (gitHead == null || !gitHead.equals(currentGitHead)) {
		update(currentGitHead);
		gitHead = currentGitHead;
		latch.countDown();
	} 
	} catch (Throwable t) {
		System.err.println("Error updating git: "+t);
        //t = StackTraceUtils.sanitizeStackTrace(t);
		t.printStackTrace(System.err)
		System.err.println("Sleeping for 1 minute before retrying");
		sleep(60*1000)
	}
},0,10,TimeUnit.SECONDS)

println "waiting for initial war deployment to complete"
latch.await()

def script= serviceContext.attributes.thisInstance["script"]
println "tomcat_start.groovy: tomcat(${instanceID}) script ${script}"

println "tomcat_start.groovy executing ${script}"

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

currJmxPort=config.jmxPort+portIncrement
println "tomcat_start.groovy: Replacing default jmx port with port ${currJmxPort}"

ant.sequential {
	exec(executable:"${script}.sh", osfamily:"unix", failonerror:true) {
        env(key:"CATALINA_HOME", value: "${home}")
        env(key:"CATALINA_BASE", value: "${home}")
        env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
		arg(value:"run")
	}
	exec(executable:"${script}.bat", osfamily:"windows", failonerror:true) { 
        env(key:"CATALINA_HOME", value: "${home}")
        env(key:"CATALINA_BASE", value: "${home}")
        env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		env(key:"CATALINA_OPTS", value:"-Dcom.sun.management.jmxremote.port=${currJmxPort} -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false")
		arg(value:"run")
	}
}
executor.shutdown();