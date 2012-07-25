/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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
import org.cloudifysource.usm.USMUtils
import org.hyperic.sigar.OperatingSystem


def installLinuxMysql(context,builder,currVendor,installScript) {
	builder.sequential {
		echo(message:"mysql_install.groovy: Chmodding +x ${context.serviceDirectory} ...")
		chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")

		echo(message:"mysql_install.groovy: Running ${context.serviceDirectory}/${installScript} os is ${currVendor}...")
		exec(executable: "${context.serviceDirectory}/${installScript}",failonerror: "true")
	}
}

def installWindowsMysql(config,osConfig,unzipDir,zipFullPath,builder) {
	builder.sequential {	
		mkdir(dir:"${unzipDir}")
		echo(message:"mysql_install.groovy get ${osConfig.zipURL} ... ")
		get(src:"${osConfig.zipURL}", dest:"${zipFullPath}", skipexisting:true)
		echo(message:"mysql_install.groovy: Unzipping ${zipFullPath} to ${context.serviceDirectory}...")
		unzip(src:"${zipFullPath}", dest:"${context.serviceDirectory}", overwrite:true)
	}
}

config=new ConfigSlurper().parse(new File('mysql-service.properties').toURL())
osConfig=USMUtils.isWindows() ? config.win64 : config.linux

context = ServiceContextFactory.getServiceContext()

def mysqlHost

if (  context.isLocalCloud()  ) {
	mysqlHost =InetAddress.getLocalHost().getHostAddress()
}
else {
	mysqlHost =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
}


println "mysql_install.groovy: mysqlHost is ${mysqlHost}"
context.attributes.thisInstance["dbHost"] = "${mysqlHost}"

context.attributes.thisInstance["dbName"] = "${config.dbName}"
println "mysql_install.groovy: dbName is ${config.dbName}"

context.attributes.thisInstance["dbUser"] = "${config.dbUser}"
println "mysql_install.groovy: dbUser is ${config.dbUser}"

context.attributes.thisInstance["dbPassW"] = "${config.dbPassW}"
println "mysql_install.groovy: dbPassW is ${config.dbPassW}"

context.attributes.thisInstance["dbPort"] = "${config.jdbcPort}"
println "mysql_install.groovy: dbPort is ${config.jdbcPort}"

context.attributes.thisInstance["postStartRequired"] = true


builder = new AntBuilder()

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
		case ["Ubuntu", "Debian", "Mint"]:		
			context.attributes.thisInstance["binFolder"]="/usr/bin"
			installLinuxMysql(context,builder,currVendor,"installOnUbuntu.sh")
			break		
		case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:	
			context.attributes.thisInstance["binFolder"]="/usr/bin"		
			installLinuxMysql(context,builder,currVendor,"install.sh")
			break					
		case ~/.*(?i)(Microsoft|Windows).*/:
			context.attributes.thisInstance["binFolder"]="${osConfig.mysqlHome}/bin"
			unzipDir = System.properties["user.home"]+ "/.cloudify/mysql"
			zipFullPath="${unzipDir}/${osConfig.zipName}"
			installWindowsMysql(config,osConfig,unzipDir,zipFullPath,builder)
			break
		default: throw new Exception("Support for ${currVendor} is not implemented")
}

println "mysql_install.groovy: End"

