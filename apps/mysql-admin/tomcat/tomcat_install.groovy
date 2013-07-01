/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def context = ServiceContextFactory.getServiceContext()
def config = new ConfigSlurper().parse(new File("${context.serviceDirectory}/tomcat-service.properties").toURL())
def instanceId = context.instanceId

println "tomcat_install.groovy: Installing tomcat..."

// Load the configuration
def catalinaHome = context.attributes.thisInstance["catalinaHome"]
def catalinaBase = context.attributes.thisInstance["catalinaBase"]
def contextPath = context.attributes.thisInstance["contextPath"]
def warUrl = context.attributes.thisService["warUrl"]

def installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceId
def applicationWar = "${installDir}/${config.warName? config.warName : new File(warUrl).name}"

//download apache tomcat
new AntBuilder().sequential {
	mkdir(dir:"${installDir}")
	
	if ( config.downloadPath =~/(?i)^(http|ftp|sftp).*/ ) {	
		echo(message:"Getting ${config.downloadPath} to ${installDir}/${config.zipName} ...")
		get(src:"${config.downloadPath}", dest:"${installDir}/${config.zipName}", skipexisting:true)
	}
	else {
		echo(message:"Copying ${context.serviceDirectory}/${config.downloadPath} to ${installDir}/${config.zipName} ...")
		copy(tofile: "${installDir}/${config.zipName}", file:"${context.serviceDirectory}/${config.downloadPath}", overwrite:false)
	}
	unzip(src:"${installDir}/${config.zipName}", dest:"${installDir}", overwrite:true)
	move(file:"${installDir}/${config.name}", tofile:"${catalinaHome}")
	chmod(dir:"${catalinaHome}/bin", perm:'+x', includes:"*.sh")
}

if ( warUrl ) {
	new AntBuilder().sequential {
		if ( warUrl.toLowerCase().startsWith("http") || warUrl.toLowerCase().startsWith("ftp")) {
			echo(message:"Getting ${warUrl} to ${applicationWar} ...")
			get(src:"${warUrl}", dest:"${applicationWar}", skipexisting:false)
		}
		else {
			echo(message:"Copying ${context.serviceDirectory}/${warUrl} to ${applicationWar} ...")
			copy(tofile: "${applicationWar}", file:"${context.serviceDirectory}/${warUrl}", overwrite:true)
		}
	}
}

def mysqlJar="${config.mysqlJar}"

new AntBuilder().sequential {		
	echo(message:"Getting ${mysqlJar} to ${catalinaBase}/lib ...")
	get(src:"${mysqlJar}", dest:"${catalinaBase}/lib", skipexisting:false)
}


// Write the context configuration
File ctxConf = new File("${catalinaBase}/conf/Catalina/localhost/${contextPath}.xml")
if (ctxConf.exists()) {
	assert ctxConf.delete()
} else {
	new File(ctxConf.getParent()).mkdirs()
}
assert ctxConf.createNewFile()
ctxConf.append("<Context docBase=\"${applicationWar}\" />")

portIncrement = 0
if (context.isLocalCloud()) {
  portIncrement = instanceId - 1
  println "tomcat_install.groovy: Replacing default tomcat port with port ${config.port + portIncrement}"
}


def dbUser
def dbPassW
def dbName
def dbServiceHost
def dbServicePort

if ( config.dbServiceName &&  "${config.dbServiceName}" != "NO_DB_REQUIRED") {
	def dbServiceName = config.dbServiceName
	println "tomcat_install.groovy: waiting for ${dbServiceName}..."
	def dbService = context.waitForService(dbServiceName, 20, TimeUnit.SECONDS)
	def dbInstances = dbService.waitForInstances(dbService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 
	dbServiceHost = dbInstances[0].hostAddress	
	println "tomcat_install.groovy: ${dbServiceName} host is ${dbServiceHost}"
	//def dbServiceInstances = context.attributes[dbServiceName].instances
		
	dbServicePort = context.attributes."${dbServiceName}".instances[1].dbPort
	println "tomcat_install.groovy: ${dbServiceName} port is ${dbServicePort}"	
	dbUser  = context.attributes."${dbServiceName}".instances[1].dbUser
	println "tomcat_install.groovy: ${dbServiceName} dbUser is ${dbUser}"
	dbPassW = context.attributes."${dbServiceName}".instances[1].dbPassW
	println "tomcat_install.groovy: ${dbServiceName} dbPassW is ${dbPassW}"
	dbName  = context.attributes."${dbServiceName}".instances[1].dbName
	println "tomcat_install.groovy: ${dbServiceName} dbName is ${dbName}"	
}


def ORIG_SERVER_RESOURCE="</GlobalNamingResources>"
def NEW_SERVER_RESOURCE="<Resource name=\"jdbc/mysqldb\" auth=\"Container\" factory=\"org.apache.tomcat.jdbc.pool.DataSourceFactory\" type=\"javax.sql.DataSource\" maxActive=\"100\" maxIdle=\"30\" maxWait=\"-1\" username=\"${dbUser}\" password=\"${dbPassW}\" driverClassName=\"com.mysql.jdbc.Driver\"  url=\"jdbc:mysql://${dbServiceHost}:${dbServicePort}/${dbName}\"/>\n${ORIG_SERVER_RESOURCE}"

def serverXmlFile = new File("${catalinaBase}/conf/server.xml") 
def serverXmlText = serverXmlFile.text
portReplacementStr = "port=\"${config.port + portIncrement}\""
ajpPortReplacementStr = "port=\"${config.ajpPort + portIncrement}\""
shutdownPortReplacementStr = "port=\"${config.shutdownPort + portIncrement}\""
serverXmlText = serverXmlText.replace("port=\"8080\"", portReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"8009\"", ajpPortReplacementStr) 
serverXmlText = serverXmlText.replace("port=\"8005\"", shutdownPortReplacementStr) 
serverXmlText = serverXmlText.replace('unpackWARs="true"', 'unpackWARs="false"')
serverXmlText = serverXmlText.replace(ORIG_SERVER_RESOURCE, NEW_SERVER_RESOURCE)
serverXmlFile.write(serverXmlText)


def ORIG_CTX="<Context>"
def NEW_CTX="${ORIG_CTX}\n<ResourceLink global=\"jdbc/mysqldb\" name=\"jdbc/mysqldb\" type=\"javax.sql.DataSource\"/>"

def contextXmlFile = new File("${catalinaBase}/conf/context.xml") 
def contextXmlText = contextXmlFile.text
contextXmlText = contextXmlText.replace(ORIG_CTX, NEW_CTX)
contextXmlFile.write(contextXmlText)

println "tomcat_install.groovy: Tomcat installation ended"
