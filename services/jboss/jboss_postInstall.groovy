import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

jbossConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()

println "jboss_postInstall.groovy: Getting the war and deploying it..."
println "jboss_postInstall.groovy: Jboss home is ${jbossConfig.home}"
println "jboss_postInstall.groovy: installation folder is ${jbossConfig.installDir}"

def portIncrement =  serviceContext.isLocalCloud() ? serviceContext.getInstanceId()-1 : 0				
def currHttpPort = jbossConfig.jbossPort + portIncrement

println "jboss_postInstall.groovy: War folder is ${jbossConfig.applicationWarFolder}"
println "jboss_postInstall.groovy: War url is ${jbossConfig.applicationWarUrl}"

new AntBuilder().sequential {	
 get(src:"${jbossConfig.applicationWarUrl}", dest:"${jbossConfig.applicationWarFolder}/${jbossConfig.petclinicMongoWar}", skipexisting:true)
 chmod(dir:"${jbossConfig.home}/bin", perm:'+x', includes:"*.sh")
 copy(tofile: "${jbossConfig.standaloneXmlFile}", file:"${serviceContext.serviceDirectory}/standalone.xml", overwrite:true)  
}


def allZeroes="0.0.0.0" 

println "jboss_postInstall.groovy: Replacing default JBoss port with port ${currHttpPort} in ${jbossConfig.standaloneXmlFile} ..."
serverXmlFile = new File("${jbossConfig.standaloneXmlFile}") 
serverXmlText = serverXmlFile.text	
portStr = "port=\"${currHttpPort}\""
serverXmlText = serverXmlText.replace('port="8080"', portStr) 

mngStr="<inet-address value=\"\${jboss.bind.address.management:127.0.0.1}\"/>"
newMmngStr="<inet-address value=\"\${jboss.bind.address.management:${allZeroes}}\"/>"

unsecureStr="<inet-address value=\"\${jboss.bind.address.unsecure:127.0.0.1}\"/>"
newUnsecureStr="<inet-address value=\"\${jboss.bind.address.unsecure:${allZeroes}}\"/>"

origOsgi="<extension module=\"org.jboss.as.osgi\"/>"
removeOsgi=""

publicStr="<inet-address value=\"\${jboss.bind.address:127.0.0.1}\"/>"           
newPublicStr="<inet-address value=\"\${jboss.bind.address:${allZeroes}}\"/>"
println "jboss_postInstall.groovy: Replacing default JBoss 127.0.0.1 with ${allZeroes} in ${jbossConfig.standaloneXmlFile} ..."
serverXmlText = serverXmlText.replace(mngStr, newMmngStr) 
serverXmlText = serverXmlText.replace(unsecureStr, newUnsecureStr) 
serverXmlText = serverXmlText.replace(origOsgi, removeOsgi) 
serverXmlFile.text = serverXmlText.replace(publicStr, newPublicStr)


println "jboss_postInstall.groovy: Post Installation event ended"
