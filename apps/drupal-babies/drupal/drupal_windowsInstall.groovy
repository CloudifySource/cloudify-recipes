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
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("drupal-service.properties").toURL())

osConfig = ServiceUtils.isWindows() ? config.win32 : config.linux


builder = new AntBuilder()

if(ServiceUtils.isWindows()) {
	unzipDir = System.properties["user.home"]+ "/.cloudify/drupal"
	downloadFile = "${unzipDir}/apache2.zip"		
	phpZipFile = "${unzipDir}/php.zip"   
	installFolder="${context.serviceDirectory}/install"	

	builder.sequential {
		echo(message:"drupal_install.groovy: Creating unzipDir ${unzipDir} ...")
		mkdir(dir:"${unzipDir}")
		echo(message:"drupal_install.groovy: Creating installFolder ${installFolder} ...")
		mkdir(dir:"${installFolder}")
		echo(message:"drupal_install.groovy: Getting ${osConfig.downloadUrl} to ${downloadFile} ...")
		get(src:"${osConfig.downloadUrl}", dest:"${downloadFile}", skipexisting:true)
		echo(message:"drupal_install.groovy: Getting ${osConfig.phpDownloadUrl} to ${phpZipFile} ...")
		get(src:"${osConfig.phpDownloadUrl}", dest:"${phpZipFile}", skipexisting:true)
	}


	apacheRootFolder="${installFolder}/Apache2"
	println "drupal_install.groovy: installFolder ${installFolder}"

	drRoot="${apacheRootFolder}/htdocs"

	phpRootFolder="${installFolder}/php"
	println "drupal_install.groovy: phpRootFolder ${phpRootFolder}"

    builder.echo(message:"drupal_install.groovy: Creating folder for phpRootFolder - ${phpRootFolder} ...")
	builder.mkdir(dir:"${phpRootFolder}")
    builder.echo(message:"drupal_install.groovy: Unzipping ${downloadFile} to ${installFolder} ...")
	builder.unzip(src:"${downloadFile}", dest:"${installFolder}", overwrite:true)	
	
	builder.echo(message:"drupal_install.groovy: Unzipping ${phpZipFile} to ${phpRootFolder} ...")
	builder.unzip(src:"${phpZipFile}", dest:"${phpRootFolder}", overwrite:true)
	
	origApacheFolder="c:/Apache2"

	configFileName="${apacheRootFolder}/conf/httpd.conf"
	configFile = new File("${configFileName}")
	configFileText = configFile.text
	println "drupal_install.groovy: Replacing ${origApacheFolder} with ${apacheRootFolder} in ${configFileName}..."
	modifiedConfig = configFileText.replace("${origApacheFolder}", "${apacheRootFolder}")


	rewriteModule="LoadModule rewrite_module modules/mod_rewrite.so"
	println "drupal_install.groovy: Replacing #${rewriteModule} with ${rewriteModule} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("#${rewriteModule}", "${rewriteModule}")


	commentedServerName="#ServerName www.example.com:80"
	newServerName="ServerName "+InetAddress.getLocalHost().getHostAddress()+":${config.apachePhpPort}"
	println "drupal_install.groovy: Replacing ${commentedServerName} with ${newServerName} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("${commentedServerName}", "${newServerName}")


	allowNone="AllowOverride None"
	dummyAllowNone="DUMMY AllowOverride"
	allowOverrideAll="AllowOverride All"
	println "drupal_install.groovy: Replacing ${allowNone} with ${allowOverrideAll} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replaceFirst("${allowNone}", "${dummyAllowNone}")
	modifiedConfig = modifiedConfig.replaceFirst("${allowNone}", "${allowOverrideAll}")
	modifiedConfig = modifiedConfig.replaceFirst("${dummyAllowNone}", "${allowNone}")

	// Php changes from here

	indexHtml="DirectoryIndex index.html"
	indexPhp="DirectoryIndex index.php index.html"
	println "drupal_install.groovy: Replacing ${indexHtml} with ${indexPhp} in ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("${indexHtml}", "${indexPhp}")

	println "drupal_install.groovy: Adding PHP5 module to ${configFileName}..."
	modifiedConfig = modifiedConfig +"# PHP5 module \n"
	modifiedConfig = modifiedConfig +"LoadModule php5_module \"${phpRootFolder}/php5apache2_2.dll\"\n"  
	modifiedConfig = modifiedConfig +"AddType application/x-httpd-php .php  \n"
	modifiedConfig = modifiedConfig +"PHPIniDir \"${phpRootFolder}\"\n"  

	origGzipType="AddType application/x-gzip .gz .tgz"
	newTypes="AddType application/x-httpd-php .php\nAddType application/x-httpd-php-source .phps\n"
	println "drupal_install.groovy: Adding ${newTypes} to ${configFileName}..."
	modifiedConfig = modifiedConfig.replace("${origGzipType}", "${origGzipType}\n${newTypes}")

	configFile.text = modifiedConfig

	println "drupal_install.groovy: Copying ${phpRootFolder}/php.ini-recommended to ${phpRootFolder}/php.ini ..."
	builder.copy(tofile:"${phpRootFolder}/php.ini",  file:"${phpRootFolder}/php.ini-recommended", overwrite:true)


	origExtension="extension_dir = \"./\""
	newExtension="extension_dir = \"${phpRootFolder}/ext\""
	phpInIFileName="${phpRootFolder}/php.ini"
	println "drupal_install.groovy: Replacing ${origExtension} to ${newExtension} in ${phpInIFileName}..."
	iniFile = new File("${phpInIFileName}")
	iniFileText = iniFile.text
	iniFileText = iniFileText.replace("${origExtension}", "${newExtension}")

	extensionList = [ "extension=php_curl.dll","extension=php_gd2.dll","extension=php_mbstring.dll","extension=php_mysql.dll","extension=php_mysqli.dll","extension=php_pdo.dll","extension=php_pdo_mysql.dll","extension=php_xmlrpc.dll" ]
	extensionList.each { 
	  println "drupal_install.groovy: Uncommenting ;${it} in ${phpInIFileName}..."
	  iniFileText = iniFileText.replace(";${it}", "${it}")
	}
}

if(ServiceUtils.isWindows()) {
  // ; For Win32 only.
  origSmtpHost="SMTP = localhost"
  newSmtpHost="SMTP = localhost"
  println "drupal_install.groovy: Replacing ${origSmtpHost} to ${newSmtpHost} in ${phpInIFileName}..."
  iniFileText = iniFileText.replace("${origSmtpHost}", "${newSmtpHost}")

  //; For Win32 only.
  origFrom=";sendmail_from = me@example.com"
  newFrom="sendmail_from = tamir@gigaspaces.com"
  println "drupal_install.groovy: Replacing ${origFrom} to ${newFrom} in ${phpInIFileName}..."  
  iniFileText = iniFileText.replace("${origFrom}", "${newFrom}")

	iniFile.text = iniFileText

	libMySqlDll="${phpRootFolder}/libmysql.dll"
		   
	builder.echo(message:"drupal_install.groovy: Copying ${libMySqlDll} to ${apacheRootFolder} ...")
	builder.copy(todir: "${apacheRootFolder}/bin",  file:"${libMySqlDll}", overwrite:true)
}	

	   	   	   

/* Deploy Dr */ 

drupalZip="${installFolder}/drupal.zip"
drupalTmpUnzipped="${installFolder}/tmpUnzipped"

drupalDefaultFolder="${drRoot}/sites/default"
drupalSettingsFilePath="${drupalDefaultFolder}/settings.php"

def mysqlInstance=context.attributes.mysql.instances[1]

mySqlHost=mysqlInstance["dbHost"]
dbName=mysqlInstance["dbName"]
dbUser=mysqlInstance["dbUser"]
dbPassW=mysqlInstance["dbPassW"]

mySqlOrigConnString="\$db_url = 'mysql://username:password@localhost/databasename';"
newMySqlConnString="\$db_url = 'mysql://${dbUser}:${dbPassW}@${mySqlHost}/${dbName}';"

allModules="${drRoot}/sites/all/modules"
allThemes="${drRoot}/sites/all/themes"

if (config.createFromScratch == true) {
	// CREATEfromScratch
    println "drupal_install.groovy: In CREATEfromScratch..."	
	
	builder.sequential {
		echo(message:"drupal_install.groovy: Getting ${config.drupalVersionUrl} to ${installFolder} ...")
		get(src:"${config.drupalVersionUrl}", dest:"${drupalZip}", skipexisting:true)	
		
		echo(message:"drupal_install.groovy: Creating drupalTmpUnzipped ${drupalTmpUnzipped} ...")
		mkdir(dir:"${drupalTmpUnzipped}")
		
		echo(message:"drupal_install.groovy: Unzipping ${drupalZip} to ${drupalTmpUnzipped} ...")
		unzip(src:"${drupalZip}", dest:"${drupalTmpUnzipped}", overwrite:true)	
	}	
		
	def unzippedFolder = new File("${drupalTmpUnzipped}")
	unzippedFolder.traverse(maxDepth:0) { rootFolder->			
		def zipContent=new File("${rootFolder}")
		println "drupal_install.groovy: Zip root folder is ${rootFolder}"
		zipContent.traverse(maxDepth:0) { unzippedFile->
			println "drupal_install.groovy: Moving ${unzippedFile} to ${drRoot}..."
			builder.move(file:"${unzippedFile}", todir:"${drRoot}", overwrite:true) 
		}
	}
		
	builder.sequential {		
		echo(message:"drupal_install.groovy: Chmodding a+w ${drupalDefaultFolder} ...")
		chmod(dir:"${drupalDefaultFolder}", perm:'a+w')
		
		echo(message:"drupal_install.groovy: Copying ${drupalDefaultFolder}/default.settings.php to ${drupalSettingsFilePath}...")
		copy(toFile: "${drupalSettingsFilePath}",  file:"${drupalDefaultFolder}/default.settings.php", overwrite:true)
		
		echo(message:"drupal_install.groovy: Chmodding a+w ${drupalSettingsFilePath} ...")
		chmod(file:"${drupalSettingsFilePath}", perm:'a+w')			
	}
	
	drupalSettingsFile = new File("${drupalSettingsFilePath}") 
    settingsText = drupalSettingsFile.text
	println "drupal_install.groovy: Replacing ${mySqlOrigConnString} with ${newMySqlConnString} in ${drupalSettingsFilePath} ..."
    drupalSettingsFile.text = settingsText.replace("${mySqlOrigConnString}", "${newMySqlConnString}")	
	
	println "drupal_install.groovy: Creating folder ${allModules} ..."
    builder.mkdir(dir:"${allModules}")

    println "drupal_install.groovy: Creating folder ${allThemes} ..."
    builder.mkdir(dir:"${allThemes}")
}	

downloadedSiteImage="${installFolder}/${config.siteImageFile}"

if (config.importSiteImage == true ) { 
	//  get zipped site files from dropbox/s3 etc.
	
	builder.sequential {	
		echo(message:"drupal_install.groovy: Getting ${config.siteImageUrl} to ${downloadedSiteImage} ...")
		get(src:"${config.siteImageUrl}", dest:"${downloadedSiteImage}", skipexisting:true)	
		
		echo(message:"drupal_install.groovy: Unzipping ${downloadedSiteImage} to ${drRoot} ...")
		unzip(src:"${downloadedSiteImage}", dest:"${drRoot}", overwrite:true)	
		
		echo(message:"drupal_install.groovy: Chmodding a+w ${drupalSettingsFilePath} ...")
		chmod(file:"${drupalSettingsFilePath}", perm:'a+w')		
	}
	
	myDbConnString="\$db_url"
	drupalSettingsFile = new File("${drupalSettingsFilePath}") 
    settingsText = drupalSettingsFile.text
	println "drupal_install.groovy: Replacing ${myDbConnString} with ${newMySqlConnString} #${myDbConnString} in ${drupalSettingsFilePath} ..."
    drupalSettingsFile.text = settingsText.replace("${myDbConnString}", "${newMySqlConnString}\n#${myDbConnString}")
}


currModuleZip="${installFolder}/currModule.zip"

config.importModules.each { 
	println it.key + " : wget " + it.value
	moduleUrl=it.value
	builder.sequential { -> moduleUrl	
		echo(message:"Getting ${moduleUrl} ...")
		get(src:"${moduleUrl}", dest:"${currModuleZip}", skipexisting:false)
		echo(message:"Unzipping ${moduleUrl} to ${allModules} ...")
		unzip(src:"${currModuleZip}", dest:"${allModules}", overwrite:true)
		//do more stuff in the DB ?
	}
}


currThemeZip="${installFolder}/currTheme.zip"

config.importThemes.each { 
	println it.key + " : wget " + it.value
	themeUrl=it.value
	builder.sequential { -> themeUrl	
		echo(message:"Getting ${themeUrl} ...")
		get(src:"${themeUrl}", dest:"${currThemeZip}", skipexisting:false)
		echo(message:"Unzipping ${themeUrl} to ${allThemes} ...")
		unzip(src:"${currThemeZip}", dest:"${allThemes}", overwrite:true)
		//do more stuff in the DB ?
	}
}


builder.sequential {
	echo(message:"drupal_install.groovy: Chmodding a-w ${drRoot}/sites/default/settings.php ...")
	chmod(file:"${drRoot}/sites/default/settings.php", perm:'a-w')
}	


println "drupal_install.groovy: Ended successfully"

