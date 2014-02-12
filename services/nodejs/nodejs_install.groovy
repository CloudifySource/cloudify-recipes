import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

println "nodejs_install.groovy: Starting ..."

def config = new ConfigSlurper().parse(new File("nodejs-service.properties").toURL())
def context = ServiceContextFactory.getServiceContext()
def instanceID = context.getInstanceId()

def ctxPath=("default" == context.applicationName)?"":"${context.applicationName}"

println "nodejs_install.groovy: Installing node.js..."

def home = "${context.serviceDirectory}"
def script = "${home}/startHelloWorldServer"

context.attributes.thisInstance["home"] = "${home}"
context.attributes.thisInstance["script"] = "${script}"

println "nodejs_install.groovy: nodejs(${instanceID}) home is ${home}"

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID


// -----------------------------------------------------------------------------------------
def installOnWindows(config,ant,context)
{
	targetFileName = "${config.downloadFolder}/" + "${config.installationFileName_win}"
	
        ant.sequential {
                        echo(message:"nodejs_install.groovy: installing on Windows...")
                        mkdir(dir:'bin')
						echo(message:"nodejs_install.groovy: Created Directory install")
						
                        mkdir(dir:"${config.downloadFolder}")
						echo(message:"nodejs_install.groovy: Created Directory ${config.downloadFolder}")
                        
                        mkdir(dir:"${config.windows_installation_path}\\bin")
						echo(message:"nodejs_install.groovy: Created Directory ${config.windows_installation_path}\\bin")


						ServiceUtils.getDownloadUtil().get("${config.downloadPath_win}", "${targetFileName}", true)
						echo(message:"nodejs_install.groovy: uploaded to ${targetFileName}")
                        
						get(src:"${config.downloadPath_win}", dest:"${targetFileName}", skipexisting:true)
						echo(message:"nodejs_install.groovy: get to ${targetFileName}")

						echo(message:"nodejs_install.groovy: ${context.serviceDirectory}")
						
						echo(message:"nodejs_install.groovy: COPY from  ${targetFileName} to ${config.windows_installation_path}/bin/node.exe")
						copy( file:"${targetFileName}", tofile:"${config.windows_installation_path}/bin/node.exe")
                }
				
        def workingDir = "${config.windows_installation_path}\\bin"
		//context.serviceDirectory[0..-2]
		
		def command = "install.bat ${workingDir}" 
		println "nodejs_install.groovy: EXEC  ${command} "
		def proc = command.execute()
		proc.waitFor()                               // Wait for the command to finish

		// Obtain status and output
		println "nodejs_install.groovy: return code: ${ proc.exitValue()}"
		println "nodejs_install.groovy: stderr: ${proc.err.text}"
		println "nodejs_install.groovy: stdout: ${proc.in.text}" 
}


// -----------------------------------------------------------------------------------------
// installation for windows with msiexec, doesn't work since windows opens a security window for running the installation

def installOnWindows_msi_version(config,ant,context)
{
	targetFileName = "${config.downloadFolder}/" + "${config.installationFileName_win}"
		command_args = '/i' + " ${context.serviceDirectory}/install/setup.msi"
        ant.sequential {
                        echo(message:"nodejs_install.groovy: installing on Windows...")
                        mkdir(dir:'install')
						echo(message:"nodejs_install.groovy: Created Directory install")
						
                        mkdir(dir:"${config.downloadFolder}")
						echo(message:"nodejs_install.groovy: Created Directory ${config.downloadFolder}")
                        
						ServiceUtils.getDownloadUtil().get("${config.downloadPath_win}", "${targetFileName}", true)
						echo(message:"nodejs_install.groovy: uploaded to ${targetFileName}")
                        
						get(src:"${config.downloadPath_win}", dest:"${targetFileName}", skipexisting:true)
						echo(message:"nodejs_install.groovy: get to ${targetFileName}")

						echo(message:"nodejs_install.groovy: ${context.serviceDirectory}")
						
						echo(message:"nodejs_install.groovy: COPY from  ${targetFileName} to install/setup.msi")
						copy( file:"${targetFileName}", tofile:"${context.serviceDirectory}/install/setup.msi")
                }
        def workingDir = context.serviceDirectory[0..-2]
		
		def command = "install.bat" 
		println "nodejs_install.groovy: EXEC  ${command} "
		def proc = command.execute()
		proc.waitFor()                               // Wait for the command to finish

		// Obtain status and output
		println "nodejs_install.groovy: return code: ${ proc.exitValue()}"
		println "nodejs_install.groovy: stderr: ${proc.err.text}"
		println "nodejs_install.groovy: stdout: ${proc.in.text}" 
}
						// exec(outputproperty:"cmdOut",
             // errorproperty: "cmdErr",
             // resultproperty:"cmdExit",
             // failonerror: "true",
             // executable: "msiexec.exe"){
                        // arg(line:"${command_args}")                        

//						arg(value:'/passive')
	//					arg(value:'/quiet')

// -----------------------------------------------------------------------------------------
// Linux installation

def installOnLinux(config,ant,context,installDir)
{
	targetFileName = "${config.downloadFolder}/" + "${config.installationFileName_linux}"
	ant.sequential 
	{
		echo(message:"nodejs_install.groovy: installing on Linux...")
		mkdir(dir:'install')
		
		echo(message:"nodejs_install.groovy: Creating Directory ${config.downloadFolder}")
		mkdir(dir:"${config.downloadFolder}")
		

		copy( file:"${context.serviceDirectory}/simpleServer.js", tofile:"./simpleServer.js")

		ServiceUtils.getDownloadUtil().get("${config.downloadPath_win}", "${targetFileName}", true)
		echo(message:"nodejs_install.groovy: uploaded to ${targetFileName}")
		chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")
		chmod(dir:".", perm:"+x", includes:"*.sh")
		exec(executable: "${context.serviceDirectory}/install.sh",failonerror: "true")
	}
}

ant = new AntBuilder()
def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
	case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:                        
			installOnLinux(config,ant,context,installDir)
			break                                        
	case ~/.*(?i)(Microsoft|Windows).*/:                
			installOnWindows(config,ant,context)
			break
	default: throw new Exception("Support for ${currVendor} is not implemented")
}

println "nodejs_install.groovy: node.js installation ended"
