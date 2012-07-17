jbossMongoConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())

println "jbossMongo_install.groovy: installation folder is ${jbossMongoConfig.installDir}"


new AntBuilder().sequential {
	mkdir(dir:jbossMongoConfig.installDir)
	get(src:"${jbossMongoConfig.downloadPath}", dest:"${jbossMongoConfig.installDir}/${jbossMongoConfig.zipName}", skipexisting:true)	
	unzip(src:"${jbossMongoConfig.installDir}/${jbossMongoConfig.zipName}", dest:"${jbossMongoConfig.installDir}", overwrite:true)
	chmod(dir:"${jbossMongoConfig.installDir}/${jbossMongoConfig.name}/bin", perm:'+x', includes:"*.sh")
}

println "jbossMongo_install.groovy: installation ended successfully"

