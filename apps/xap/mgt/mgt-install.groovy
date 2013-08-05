import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

serviceContext = ServiceContextFactory.getServiceContext()

config = new ConfigSlurper().parse(new File("mgt-service.properties").toURL())

instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.gigaspaces/${config.serviceName}" + instanceID

home = System.properties["user.home"] +"/gshome"


serviceContext.attributes.thisApplication["home"] = "${home}"
println "mgt_install.groovy: gigaspaces(${instanceID}) home is ${home}"

serviceContext.attributes.thisApplication["pc-lab${instanceID}"] = InetAddress.localHost.hostAddress

mgtService = serviceContext.waitForService("mgt", 20, TimeUnit.SECONDS)
if (mgtService == null) {
    throw new IllegalStateException("mgt service not found.");
}
mgtHostInstances = mgtService.waitForInstances(mgtService.numberOfPlannedInstances, 30, TimeUnit.SECONDS)

if (mgtHostInstances == null) {
    throw new IllegalStateException("mgtHostInstances not found.");
}

def locators = "";
mgtHostInstances.each {
    locators += it.hostAddress + ":" + "${config.port}" + ","
}

locators = locators.toString().substring(0, locators.toString().length() -1)
serviceContext.attributes.thisApplication["locators"] = "${locators}"

builder = new AntBuilder()
builder.sequential {
	mkdir(dir:"${installDir}")
	get(src:"${config.downloadPath}", dest:"${installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${installDir}/${config.zipName}", dest:"${installDir}", overwrite:true)
}

println "mgt_install.groovy: gigaspaces(${instanceID}) moving ${installDir}/${config.name} to ${home}..."

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

