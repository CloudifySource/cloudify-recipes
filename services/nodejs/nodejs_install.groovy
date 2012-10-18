import org.cloudifysource.dsl.context.ServiceContextFactory
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("nodejs-service.properties").toURL())
serviceContext = ServiceContextFactory.getServiceContext()
osConfig = !ServiceUtils.isWindows() ? config.unix :
    (System.properties["os.arch"]?.indexOf("64") != -1 ? config.win64 : config.win32)
instanceID = serviceContext.getInstanceId()
installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID
def home = "${serviceContext.serviceDirectory}/${config.name}"

builder = new AntBuilder()
builder.sequential {
    mkdir(dir:"${installDir}")
}

if(ServiceUtils.isWindows()) {
    builder.get(src:"${osConfig.downloadPath}", dest:"${home}.exe", skipexisting:true)
} else {
    builder.get(src:"${osConfig.downloadPath}", dest:"${installDir}/${osConfig.filename}", skipexisting:true)
    builder.untar(src:"${installDir}/${osConfig.filename}", dest:"${installDir}", overwrite:true)
    builder.move(file:"${installDir}/${osConfig.filename}", tofile:"${home}")
    builder.chmod(dir:"${home}/bin", perm:'+x', includes:"*.sh")
}

if ( "${config.jsFileName}" != "" ) {
    new AntBuilder().sequential {
        copy(todir:"${installDir}", file:"${config.jsFileName}")
        //get(src:"${config.jsFileName}", dest:"${applicationjsFileName}", skipexisting:false)
    }
}

println "nodejs_install.groovy: node.js installation ended"