import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

serviceContext = ServiceContextFactory.getServiceContext()

config = new ConfigSlurper().parse(new File("pu-service.properties").toURL())

instanceID = serviceContext.getInstanceId() + 2

installDir = System.properties["user.home"]+ "/.gigaspaces/${config.serviceName}" + instanceID

home = System.properties["user.home"] +"/gshome"


serviceContext.attributes.thisService["home"] = "${home}"
println "pu_install.groovy: gigaspaces(${instanceID}) home is ${home}"

serviceContext.attributes.thisApplication["pc-lab${instanceID}"] = InetAddress.localHost.hostAddress

builder = new AntBuilder()
builder.sequential {
	mkdir(dir:"${installDir}")
	get(src:"${config.downloadPath}", dest:"${installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${installDir}/${config.zipName}", dest:"${installDir}", overwrite:true)
}

println "pu_install.groovy: gigaspaces(${instanceID}) moving ${installDir}/${config.name} to ${home}..."

builder.sequential {
	move(file:"${installDir}/${config.name}", tofile:"${home}")
}

builder.sequential {	
	chmod(dir:".", perm:'+x', includes:"*.sh")
}

gslicenseAtCloudify =  System.properties["user.home"] +"/gigaspaces/xap-license/gslicense.xml"
builder.sequential {
    copy(file:"${gslicenseAtCloudify}", todir:"${home}")
}

println "gigaspaces_install.groovy: gigaspaces(${instanceID}) ended"

