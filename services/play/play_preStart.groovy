/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit
import org.cloudifysource.domain.context.ServiceInstance;

config = new ConfigSlurper().parse(new File("play-service.properties").toURL())

serviceContext = ServiceContextFactory.getServiceContext()

def useDB
def dbHost
def dbPort

if ( !(config.dbServiceName) ||  "${config.dbServiceName}"=="NO_DB_REQUIRED") {
	println "play_preStart.groovy: Your application doesn't use a database"
	useDB=false
}
else { 
	useDB = true
		
	if ( !(config.dbHost) ||  "${config.dbHost}"=="DB_INSTALLED_BY_CLOUDIFY") {
		dbService = serviceContext.waitForService(config.dbServiceName, 180, TimeUnit.SECONDS)
		if (dbService == null) {
			throw new IllegalStateException("${config.dbServiceName} service not found.");
		}
		ServiceInstance[] dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 180, TimeUnit.SECONDS)

		if (dbInstances == null) {
			throw new IllegalStateException("dbInstances not found.");
		}

		dbHost = dbInstances[0].getHostAddress()
		println "play_preStart.groovy: dbHost is ${dbHost}"

		def dbServiceInstances=serviceContext.attributes[config.dbServiceName].instances                   
		dbServiceInstance=dbServiceInstances[1]				
		dbPort=dbServiceInstance["dbPort"]
		println "play_preStart.groovy: dbPort is ${dbPort}"
	}
	else {
		dbHost = config.dbHost
		println "play_preStart.groovy: Using (external db) dbHost : ${dbHost}"
		dbPort = config.dbPort
		println "play_preStart.groovy: Using (external db) dbPort : ${dbPort}"
	}
}

applicationNameStr="application.name"

playRootFolder=serviceContext.serviceDirectory + "/${config.name}"
println "play_preStart.groovy: playRootFolder is ${playRootFolder}"

applicationRootFolder="${playRootFolder}/playApps/${config.applicationName}"
println "play_preStart.groovy: applicationRootFolder is ${applicationRootFolder}"

confFolder="${applicationRootFolder}/conf"
println "play_preStart.groovy: confFolder is ${confFolder}"

frameworkFolder="${playRootFolder}/framework"
println "play_preStart.groovy: frameworkFolder is ${frameworkFolder}"

confFilePath = "${confFolder}/application.conf"
println "play_preStart.groovy: confFilePath is ${confFilePath}"

def confFile = new File(confFilePath)
text = confFile.text

if ( config.useAkka ) { 
	println "play_preStart.groovy: Using akka ..."
	akkaLogLevelStr="akka.loglevel=DEBUG"
	akkaParserTimeoutStr="play.akka.actor.retrieveBodyParserTimeout=30 second"
	akkaTypedTimeoutStr="promise.akka.actor.typed.timeout=30s"
	text = text.replace("${applicationNameStr}", "${akkaLogLevelStr}\n${applicationNameStr}")
	text = text.replace("${applicationNameStr}", "${akkaParserTimeoutStr}\n${applicationNameStr}")
	text = text.replace("${applicationNameStr}", "${akkaTypedTimeoutStr}\n${applicationNameStr}")
}

if ( config.productionMode ) { 
	println "play_preStart.groovy: Using productionMode ..."
	applicationModeStr="application.mode=prod"
	text = text.replace("${applicationNameStr}", "${applicationModeStr}\n${applicationNameStr}")
}	

if ( useDB ) {
	println "play_preStart.groovy: Setting DB host and port in ${confFilePath} ..."
	
	if ( "${config.dbServiceName}" == "mysql" ) { 
		dbDriverStr="db.default.driver=com.mysql.jdbc.Driver"
		dbUrlStr="db.default.url=\"jdbc:mysql://${dbHost}:${dbPort}/${config.dbName}\""
	}
	else {
		println "play_preStart.groovy: You need to implement the dbDriverStr and dbUrlStr according to your db type : ${dbServiceName}"
	}
	
	dbUserStr="db.default.user=${config.dbUser}"
	dbPassStr="db.default.password=${config.dbPassW}"
	dbTimeoutStr="db.default.pool.timeout=30000"
	text = text.replace("db.default.driver=org.h2.Driver", "${dbDriverStr}")		
	text = text.replace("db.default.url=\"jdbc:h2:mem:play\"","${dbUrlStr}\n${dbUserStr}\n${dbPassStr}\n${dbTimeoutStr}")
		
	
	if ( config.applyEvolutions ) { 
		println "play_preStart.groovy: Setting applyEvolutions=true in ${confFilePath} ..."
		text = text + "\n\napplyEvolutions.default=true\n"
	}
	
	if ( config.useLoadBalancer ) { 
		println "play_preStart.groovy: Setting XForwardedSupport in ${confFilePath} ..."	
		xFwdSupportStr="trustxforwarded=true"
		text = text + "\n\n${xFwdSupportStr}\n"
	}	
		

	if ( config.replace1Sql ) { 		
		new1Sql="${confFolder}/evolutions/default/1.sql"
		new AntBuilder().sequential {			
			echo(message:"Copying 1.sql to ${new1Sql} ...")
			copy(tofile: "${new1Sql}", file:"1.sql", overwrite:true)	
		}
	}	
}


confFile.text = text

if ( config.useAkka ) { 
	referenceConfPath="${frameworkFolder}/src/play/src/main/resources/reference.conf"
	println "play_preStart.groovy: Replacing 1.0 with 24 in ${referenceConfPath} - this increases timeout"
	def referenceConfFile = new File(referenceConfPath)
	referenceText = referenceConfFile.text
	
	if ( config.akkaParallelismFactor ) { 
		referenceText = referenceText.replaceAll("1.0","${config.akkaParallelismFactor}")
	}
	
	if ( config.retrieveBodyParserTimeout ) { 
		referenceText = referenceText.replaceAll("1 second","${config.retrieveBodyParserTimeout}")
	}
	referenceConfFile.text = referenceText
}	

if ( useDB ) { 
	buildFilePath = "${applicationRootFolder}/project/Build.scala"
	def buildfile = new File(buildFilePath)
	text = buildfile.text
	if ( "${config.dbServiceName}" == "mysql" ) { 		
		println "play_preStart.groovy: Setting MySQL driver dependencies in " + buildFilePath + "..."			
		text = text.replace("// Add your project dependencies here,", "\"mysql\" % \"mysql-connector-java\" % \"5.1.18\"")
		buildfile.text = text
	}
	else {
		println "play_preStart.groovy: You need to implement the project dependencies according to your db type : ${dbServiceName}"
	}
}	




