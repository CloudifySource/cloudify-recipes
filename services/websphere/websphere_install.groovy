import groovy.util.ConfigSlurper

println "websphere_install.groovy: Installing..."
websphereConfig = new ConfigSlurper().parse(new File("websphere-service.properties").toURL())

println "websphere_install.groovy: Unzipping to folder ${websphereConfig.wasUnzippedFolder}"

new AntBuilder().sequential {
	mkdir(dir:"${websphereConfig.installDir}")			
	mkdir(dir:"${websphereConfig.unzipTofolder}")
	
	echo(message:"Getting ${websphereConfig.gzipDownloadPath} ...")
	get(src:"${websphereConfig.gzipDownloadPath}", dest:"${websphereConfig.rootFolder}/${websphereConfig.gzipName}", skipexisting:true)
	
	echo(message:"Untarring ${websphereConfig.gzipFullPath}")
	exec(executable:"tar", osfamily:"unix") {
		arg(value:"-z")  
		arg(value:"-p")  
		arg(value:"-xvf")  
		arg(value:"${websphereConfig.gzipFullPath}")
		arg(value:"--overwrite")
		arg(value:"-C")
		arg(value:"${websphereConfig.unzipTofolder}/")
	}
		
	echo(message:"Copying install to ${websphereConfig.wasUnzippedFolder}")
	copy(todir: "${websphereConfig.wasUnzippedFolder}/", file:"overrides-linux/install", overwrite:true)
	
	echo(message:"Copying responsefile to ${websphereConfig.wasUnzippedFolder}")
	copy(todir: "${websphereConfig.wasUnzippedFolder}/", file:"overrides-linux/responsefile.base.txt", overwrite:true)
	
	chmod(dir:"${websphereConfig.wasUnzippedFolder}", perm:'+x', includes:"*")		
	echo(message:"End of chmodding ${websphereConfig.wasUnzippedFolder}")
}

println "websphere_install.groovy: Setting ${websphereConfig.responsefile} in installation directory ${websphereConfig.installDir} ..."
responseFile = new File("${websphereConfig.responsefile}") 
responseFileText=responseFile.text

println "websphere_install.groovy: Setting ${websphereConfig.installDir}/ in ${websphereConfig.responsefile} ..."
responseFileText=responseFileText.replace("REPLACE_WITH_INSTALL_DIR","${websphereConfig.installDir}/") 

println "websphere_install.groovy: Setting user ${websphereConfig.adminUser} in ${websphereConfig.responsefile} ..."
responseFileText=responseFileText.replace("REPLACE_WITH_ADMIN_USER","${websphereConfig.adminUser}") 

println "websphere_install.groovy: Setting password ${websphereConfig.adminPassword} in ${websphereConfig.responsefile} ..."
responseFileText=responseFileText.replace("REPLACE_WITH_ADMIN_PASSWORD","${websphereConfig.adminPassword}") 

println "websphere_install.groovy: Setting ${websphereConfig.startingPort} in ${websphereConfig.responsefile} ..."
responseFileText=responseFileText.replace("STARTING_PORT","${websphereConfig.startingPort}") 
responseFile.text=responseFileText


println "websphere_install.groovy: Invoking ${websphereConfig.wasUnzippedFolder}/install ..."

new AntBuilder().sequential {
	echo(message:"Chmodding ${websphereConfig.wasUnzippedFolder} ...")
	chmod(dir:"${websphereConfig.wasUnzippedFolder}", perm:'+x', includes:"*")	
	
	echo(message:"Executing ${websphereConfig.wasUnzippedFolder}/install with responsefile.base.txt ...")
	exec(executable:"${websphereConfig.wasUnzippedFolder}/install", osfamily:"unix") {
		arg(value:"-options")  
		arg(value:"${websphereConfig.wasUnzippedFolder}/responsefile.base.txt")
		arg(value:"-silent")
	}
	
	echo(message:"Chmodding ${websphereConfig.samplesDir} ...")
	chmod(dir:"${websphereConfig.samplesDir}", perm:'+x', includes:"*")		
	chmod(dir:"${websphereConfig.samplesDir}/bin", perm:'+x', includes:"*")
	
	echo(message:"Executing ${websphereConfig.samplesScript} with AlbumCatalog ...")
	exec(executable:"${websphereConfig.samplesScript}", osfamily:"unix") {
		arg(value:"-server")
		arg(value:"server1")
		arg(value:"-samples")
		arg(value:"AlbumCatalog")
	}
}

println "websphere_install.groovy: Installation ended successfully"

