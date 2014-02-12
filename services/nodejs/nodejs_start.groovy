import java.util.concurrent.TimeUnit
import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

def config=new ConfigSlurper().parse(new File("nodejs-service.properties").toURL())
println "nodejs_start.groovy: Run Nodejs hello world server"

def serviceContext = ServiceContextFactory.getServiceContext()
def instanceID = serviceContext.getInstanceId()
def home= serviceContext.attributes.thisInstance["home"]
def script= serviceContext.attributes.thisInstance["script"]

println "nodejs_start.groovy: nodejs(${instanceID}) script ${script}"

ant = new AntBuilder()
def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
                case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:                        
                        builder = new AntBuilder()
						builder.sequential {
							echo(message:"nodejs_start.groovy: Running ${script} ...")
							exec(executable:"${script}.sh", failonerror: "true")  {
							arg(value:"${config.demoApplicationPort}")
							}
						}
                        break                                        
                case ~/.*(?i)(Microsoft|Windows).*/:                
                        builder = new AntBuilder()
						builder.sequential {
							echo(message:"nodejs_start.groovy: Running ${script} ...")
							exec(executable:"${script}.bat", failonerror: "true")  {
							arg(value:"${config.demoApplicationPort}")
							}
						}
						break
                default: throw new Exception("Support for ${currVendor} is not implemented")
}