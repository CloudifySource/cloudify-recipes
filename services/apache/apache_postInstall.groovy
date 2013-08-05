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
import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("apache-service.properties").toURL())


def portIncrement =  context.isLocalCloud() ? context.getInstanceId()-1 : 0			
def currentPort = config.port + portIncrement

def useDB = context.attributes.thisInstance["useDB"]
def dbType = context.attributes.thisInstance["dbType"]


builder = new AntBuilder()


def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
def confScript
def origProxyBalancerPath
def proxyBalancerName
def isLinux
switch (currVendor) {
	case ["Ubuntu", "Debian", "Mint"]:			
		confScript="${context.serviceDirectory}/configureApacheConfOnUbuntu.sh"
		isLinux=true
		break		
	case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:			
		confScript="${context.serviceDirectory}/configureApacheConf.sh"	
		isLinux=true
		break					
	case ~/.*(?i)(Microsoft|Windows).*/:		
		confScript=""
		isLinux=false
		break
	default: throw new Exception("Support for ${currVendor} is not implemented")
}



def zipContentLevel
if ( context.attributes.thisInstance["zipContentLevel"] != null ) {
	/* This means that the zip's content is in the root folder */
	zipContentLevel = context.attributes.thisInstance["zipContentLevel"]				
}
else {
	/* This means that the zip's content is in 2nd level folder */
	zipContentLevel = "1"
}

if ( isLinux ) {
	
	builder.sequential {		
		echo(message:"apache_install.groovy: Running ${confScript} os is ${currVendor}...")
		exec(executable: "${confScript}",failonerror: "true") {
			arg(value:"80")		
			arg(value:"${currentPort}")			
			arg(value:"${config.php}")			
			arg(value:"${config.applicationZipUrl}")			
			arg(value:"${zipContentLevel}")			
		}	
	}
}
else {	
	osConfig = config.win32
	installFolder="${context.serviceDirectory}/install"

	builder = new AntBuilder()


	apacheRootFolder="${installFolder}/Apache2"
	println "apache_postInstall.groovy: installFolder ${installFolder}"

	documentRoot="${apacheRootFolder}/htdocs"

	phpRootFolder="${installFolder}/php"
	println "apache_postInstall.groovy: phpRootFolder ${phpRootFolder}"

	builder.copy(todir: "${documentRoot}",  file:"phpinfo.php", overwrite:true)

	origApacheFolder="c:/Apache2"

	configFileName="${apacheRootFolder}/conf/httpd.conf"
	configFile = new File("${configFileName}")
	configFileText = configFile.text
	println "apache_postInstall.groovy: Replacing ${origApacheFolder} with ${apacheRootFolder} in ${configFileName}..."
	modifiedConfig = configFileText.replace("${origApacheFolder}", "${apacheRootFolder}")


	rewriteModule="LoadModule rewrite_module modules/mod_rewrite.so"
	println "apache_postInstall.groovy: Replacing #${rewriteModule} with ${rewriteModule} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("#${rewriteModule}", "${rewriteModule}")


	commentedServerName="#ServerName www.example.com:80"
	newServerName="ServerName "+InetAddress.getLocalHost().getHostAddress()+":${currentPort}"
	println "apache_postInstall.groovy: Replacing ${commentedServerName} with ${newServerName} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("${commentedServerName}", "${newServerName}")


	allowNone="AllowOverride None"
	dummyAllowNone="DUMMY AllowOverride"
	allowOverrideAll="AllowOverride All"
	println "apache_postInstall.groovy: Replacing ${allowNone} with ${allowOverrideAll} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replaceFirst("${allowNone}", "${dummyAllowNone}")
	modifiedConfig = modifiedConfig.replaceFirst("${allowNone}", "${allowOverrideAll}")
	modifiedConfig = modifiedConfig.replaceFirst("${dummyAllowNone}", "${allowNone}")

	// Php changes from here

	indexHtml="DirectoryIndex index.html"
	indexPhp="DirectoryIndex index.php index.html"
	println "apache_postInstall.groovy: Replacing ${indexHtml} with ${indexPhp} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("${indexHtml}", "${indexPhp}")

	println "apache_postInstall.groovy: Adding PHP5 module to ${configFileName}..."
	modifiedConfig = modifiedConfig +"# PHP5 module \n"
	modifiedConfig = modifiedConfig +"LoadModule php5_module \"${phpRootFolder}/php5apache2_2.dll\"\n"  
	modifiedConfig = modifiedConfig +"AddType application/x-httpd-php .php  \n"
	modifiedConfig = modifiedConfig +"PHPIniDir \"${phpRootFolder}\"\n"  

	origGzipType="AddType application/x-gzip .gz .tgz"
	newTypes="AddType application/x-httpd-php .php\nAddType application/x-httpd-php-source .phps\n"
	println "apache_postInstall.groovy: Adding ${newTypes} to ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("${origGzipType}", "${origGzipType}\n${newTypes}")

	configFile.text = modifiedConfig

	println "apache_postInstall.groovy: Copying ${phpRootFolder}/php.ini-recommended to ${phpRootFolder}/php.ini ..."
	builder.copy(tofile:"${phpRootFolder}/php.ini",  file:"${phpRootFolder}/php.ini-recommended", overwrite:true)


	origExtension="extension_dir = \"./\""
	newExtension="extension_dir = \"${phpRootFolder}/ext\""
	phpInIFileName="${phpRootFolder}/php.ini"
	println "apache_postInstall.groovy: Replacing ${origExtension} to ${newExtension} in ${phpInIFileName}..."
	iniFile = new File("${phpInIFileName}")
	iniFileText = iniFile.text
	iniFileText = iniFileText.replace("${origExtension}", "${newExtension}")

	
	extensionList = [ "extension=php_curl.dll","extension=php_gd2.dll","extension=php_mbstring.dll","extension=php_pdo.dll","extension=php_xmlrpc.dll" ]
	extensionList.each { 
	  println "apache_postInstall.groovy: Uncommenting ;${it} in ${phpInIFileName}..."
	  iniFileText = iniFileText.replace(";${it}", "${it}")
	}
	
	if ( useDB ) { 
		if ( dbType == "mysql" ) {
			mysqlExtensionList = [ "extension=php_mysql.dll","extension=php_mysqli.dll","extension=php_pdo_mysql.dll" ]
			mysqlExtensionList.each { 
				println "apache_postInstall.groovy: Uncommenting ;${it} in ${phpInIFileName}..."
				iniFileText = iniFileText.replace(";${it}", "${it}")
			}
		}
	}
	
	
	iniFile.text = iniFileText
	 
	if ( useDB ) { 
		if ( dbType == "mysql" ) {
			libMySqlDll="${phpRootFolder}/libmysql.dll"
			   
			builder.echo(message:"apache_postInstall.groovy: Copying ${libMySqlDll} to ${apacheRootFolder} ...")
			builder.copy(todir: "${apacheRootFolder}/bin",  file:"${libMySqlDll}", overwrite:true)
		}
		else {
			 // Implement something else (e.g. : for postgres)
		}
	}
		 
	
}
