import org.cloudifysource.dsl.context.ServiceContextFactory
println "nodejs_stop.groovy: About to stop nodejs..."

def serviceContext = ServiceContextFactory.getServiceContext()

def instanceID=serviceContext.instanceId
def home= serviceContext.attributes.thisInstance["home"]
println "nodejs_stop.groovy: nodejs(${instanceID}) home ${home}"

def script= serviceContext.attributes.thisInstance["script"]
if (script) {
println "nodejs_stop.groovy: nodejs(${instanceID}) script ${script}"


println "nodejs_stop.groovy: executing command ${script}"
new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix") {
        env(key:"CATALINA_HOME", value: "${home}")
    env(key:"CATALINA_BASE", value: "${home}")
    env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		arg(value:"stop")
	}
	exec(executable:"${script}.bat", osfamily:"windows"){
        env(key:"CATALINA_HOME", value: "${home}")
    env(key:"CATALINA_BASE", value: "${home}")
    env(key:"CATALINA_TMPDIR", value: "${home}/temp")
		arg(value:"stop")
	}
}

println "nodejs_stop.groovy: nodejs is stopped"
}