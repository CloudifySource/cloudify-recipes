import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

serviceContext = ServiceContextFactory.getServiceContext()

config = new ConfigSlurper().parse(new File("webui-service.properties").toURL())

instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.gigaspaces/${config.serviceName}" + instanceID

home = System.properties["user.home"] +"/gshome"


serviceContext.attributes.thisService["home"] = "${home}"
println "webui_install.groovy: gigaspaces(${instanceID}) home is ${home}"

builder = new AntBuilder()
builder.sequential {
	mkdir(dir:"${installDir}")
	get(src:"${config.downloadPath}", dest:"${installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${installDir}/${config.zipName}", dest:"${installDir}", overwrite:true)
}

println "webui_install.groovy: gigaspaces(${instanceID}) moving ${installDir}/${config.name} to ${home}..."

builder.sequential {
	move(file:"${installDir}/${config.name}", tofile:"${home}")
}

builder.sequential {	
	chmod(dir:".", perm:'+x', includes:"*.sh")
}

println "gigaspaces_install.groovy: gigaspaces(${instanceID}) ended"

