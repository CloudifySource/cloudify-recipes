import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def serviceName = serviceContext.getServiceName()
println "tomcat_start.groovy: This ${serviceName} instance ID is ${instanceID}"
def config=new ConfigSlurper().parse(new File("${serviceName}-service.properties").toURL())

def home= serviceContext.attributes.thisInstance["home"]
def gitexec= serviceContext.attributes.thisInstance["git"]
def mvnexec= serviceContext.attributes.thisInstance["mvn"]

serviceContext.attributes.thisInstance["git-head"]=config.gitHead;

def git = { gitargs ->
 new AntBuilder().sequential {
	echo("git ${gitargs}")
	exec(executable:gitexec, dir:"${home}/${config.applicationSrcFolder}", failonerror:true) {
	   for (gitarg in gitargs.split(" ")) {
		arg(value:gitarg)
	   }
	  }
 }
}

def mvn = {mvnargs ->
 new AntBuilder().sequential {
  echo("mvn ${mvnargs}")
  exec(executable:mvnexec, dir:"${home}/${config.applicationSrcFolder}", failonerror:true) {
   for (mvnarg in mvnargs.split(" ")) {
    arg(value:mvnarg)
   }
  }
 }
}

def update= {

 git("checkout -q master")
 git("pull")
 git("branch -f build ${->serviceContext.attributes.thisInstance["git-head"]}")
 git("checkout -q build")
 mvn("clean package")
 
 new AntBuilder().sequential {
  echo("deploying war file")
  copy(todir: "${home}/webapps", file:"${home}/${config.applicationSrcFolder}/target/${config.applicationWarFilename}", overwrite:true)
 }
}

println "tomcat_start.groovy: tomcat(${instanceID}) home ${home}"

update()

def script= serviceContext.attributes.thisInstance["script"]
println "tomcat_start.groovy: tomcat(${instanceID}) script ${script}"

println "tomcat_start.groovy executing ${script}"

portIncrement = 0
if (serviceContext.isLocalCloud()) {
  portIncrement = instanceID - 1  
}

currJmxPort=config.jmxPort+portIncrement
println "tomcat_start.groovy: Replacing default jmx port with port ${currJmxPort}"

new AntBuilder().sequential {
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
