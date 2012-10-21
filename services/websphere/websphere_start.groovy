import org.cloudifysource.dsl.context.ServiceContextFactory
import groovy.util.ConfigSlurper
import java.util.concurrent.TimeUnit

println "websphere_start.groovy: Setting websphereConfig..."
websphereConfig = new ConfigSlurper().parse(new File("websphere-service.properties").toURL())

println "websphere_start.groovy: Setting serviceContext ..."

serviceContext = ServiceContextFactory.getServiceContext()
println "websphere_start.groovy: Got serviceContext..."

println "Copying ${websphereConfig.startScriptName} to ${websphereConfig.startScript} ..."

new AntBuilder().sequential {	
	copy(todir: "${websphereConfig.installBin}/", file:"overrides-linux/${websphereConfig.startScriptName}", overwrite:true)
}

println "Chmodding ${websphereConfig.installBin} ..."

new AntBuilder().sequential {	
	chmod(dir:"${websphereConfig.installBin}", perm:'+x', includes:"*")
}

new AntBuilder().sequential {	
	echo(message:"Creating a new jacl installation script ${websphereConfig.newInstallAppScript} based on ${websphereConfig.origInstallAppScript} ...")
	copy(tofile: "${websphereConfig.newInstallAppScript}", file:"${websphereConfig.origInstallAppScript}", overwrite:true)
}

installScriptFile = new File("${websphereConfig.newInstallAppScript}") 
installScriptText=installScriptFile.text
println "Replacing APPLICATION_CONTEXT_ROOT with ${websphereConfig.applicationContextRoot} in ${websphereConfig.newInstallAppScript} ..."
installScriptText=installScriptText.replace("APPLICATION_CONTEXT_ROOT","${websphereConfig.applicationContextRoot}") 
println "Replacing APPLICATION_WAR_OR_EAR_NAME with ${websphereConfig.applicationWarOrEarName} in ${websphereConfig.newInstallAppScript} ..."
installScriptText=installScriptText.replace("APPLICATION_WAR_OR_EAR_NAME","${websphereConfig.applicationWarOrEarName}") 
println "Replacing APPLICATION_NAME with ${websphereConfig.applicationName} in ${websphereConfig.newInstallAppScript} ..."
installScriptText=installScriptText.replace("APPLICATION_NAME","${websphereConfig.applicationName}")
println "Replacing ROOT_FOLDER with ${websphereConfig.rootFolder} in ${websphereConfig.newInstallAppScript} ..."
installScriptText=installScriptText.replace("ROOT_FOLDER","${websphereConfig.rootFolder}") 
installScriptFile.text=installScriptText


new AntBuilder().sequential {	
	echo(message:"Creating a new jacl start script ${websphereConfig.newStartAppScript} based on ${websphereConfig.origStartAppScript} ...")
	copy(tofile: "${websphereConfig.newStartAppScript}", file:"${websphereConfig.origStartAppScript}", overwrite:true)
}

newStartScriptFile = new File("${websphereConfig.newStartAppScript}") 
newStartScriptFileText=newStartScriptFile.text
println "Replacing APPLICATION_NAME with ${websphereConfig.applicationName} in ${websphereConfig.newStartAppScript} ..."
newStartScriptFileText=newStartScriptFileText.replace("APPLICATION_NAME","${websphereConfig.applicationName}") 
newStartScriptFile.text=newStartScriptFileText


new AntBuilder().sequential {	
	echo(message:"Creating a new jacl uninstallation script ${websphereConfig.newUninstallAppScript} based on ${websphereConfig.origUninstallAppScript} ...")
	copy(tofile: "${websphereConfig.newUninstallAppScript}", file:"${websphereConfig.origUninstallAppScript}", overwrite:true)
}

uninstallScriptFile = new File("${websphereConfig.newUninstallAppScript}") 
uninstallScriptText=uninstallScriptFile.text
println "Replacing APPLICATION_NAME with ${websphereConfig.applicationName} in ${websphereConfig.newUninstallAppScript} ..."
uninstallScriptText=uninstallScriptText.replace("APPLICATION_NAME","${websphereConfig.applicationName}") 
uninstallScriptFile.text=uninstallScriptText



println "websphere_start.groovy: Executing ${websphereConfig.startScript} ..."

new AntBuilder().sequential {
	exec(executable:"${websphereConfig.startScript}", osfamily:"unix") {        
		arg(value:"server1")
		arg(value:"-quiet")			
	}
	
	echo(message:"Getting ${websphereConfig.applicationWarDownloadPath} ...")
	get(src:"${websphereConfig.applicationWarDownloadPath}",dest:"${websphereConfig.applicationWarPath}",   skipexisting:true)
	
	echo(message:"Chmodding ${websphereConfig.installBin} ...")
	chmod(dir:"${websphereConfig.installBin}", perm:'+x', includes:"*")
	
	echo(message:"Executing ${websphereConfig.wsadminScript} with ${websphereConfig.newInstallAppScript} ...")
	exec(executable:"${websphereConfig.wsadminScript}", osfamily:"unix") {
		arg(value:"-profile")
		arg(value:"${websphereConfig.newInstallAppScript}")
		arg(value:"-user")
		arg(value:"${websphereConfig.adminUser}")
		arg(value:"-password")
		arg(value:"${websphereConfig.adminPassword}")		
	}
	
	echo(message:"Executing ${websphereConfig.wsadminScript} with ${websphereConfig.newStartAppScript} ...")
	exec(executable:"${websphereConfig.wsadminScript}", osfamily:"unix") {
		arg(value:"-profile")
		arg(value:"${websphereConfig.newStartAppScript}")
		arg(value:"-user")
		arg(value:"${websphereConfig.adminUser}")
		arg(value:"-password")
		arg(value:"${websphereConfig.adminPassword}")		
	}	
}

sleep(Integer.MAX_VALUE)
println "websphere_start.groovy: Ended successfully"







