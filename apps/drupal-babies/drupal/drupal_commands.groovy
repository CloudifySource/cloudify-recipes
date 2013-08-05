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
import org.cloudifysource.dsl.utils.ServiceUtils;


println "drupal_commands.groovy: Starting ..."

config = new ConfigSlurper().parse(new File("drupal-service.properties").toURL())
context = ServiceContextFactory.getServiceContext()

osConfig = ServiceUtils.isWindows() ? config.win32 : config.linux

def printUsage() {
	def msg = "This custom command enables users to upload a module ,theme or file to their site. " + "\n"
	msg += "	Usage :  " + "\n"
	msg += " 		invoke drupal cmd upload [moudle|theme] zip_full_url_path" + "\n"
	msg += " 		  or" + "\n"
	msg += " 		invoke drupal cmd upload file file_full_url_path  destination_folder_relative_to_home" + "\n"
	msg += " 	Examples: " + "\n"
	msg += " 		invoke drupal cmd upload module http://ftp.drupal.org/files/projects/views-7.x-3.5.zip" + "\n"
	msg += " 		invoke drupal cmd upload module http://my1stStorageSite.com/myNewModule-7.x-1.4.zip" + "\n"
	msg += " 		invoke drupal cmd upload theme http://ftp.drupal.org/files/projects/sasson-7.x-2.7.zip" + "\n"
	msg += " 		invoke drupal cmd upload theme http://my2ndStorageSite.com/myNewTheme-7.x-2.2.zip" + "\n"
	msg += " 		invoke drupal cmd upload file http://my3rdStoragSite.com/myFile sites/default/files/" + "\n"
	println msg
	return msg
	
}

def downloadUnzipAndCopy(builder,zipFile,destFolder) {

	def tmpZip="${destFolder}/currZip.zip"

	builder.sequential { -> zipFile	
		echo(message:"Getting ${zipFile} ...")
		get(src:"${zipFile}", dest:"${tmpZip}", skipexisting:false)
		echo(message:"Unzipping ${zipFile} to ${destFolder} ...")
		unzip(src:"${tmpZip}", dest:"${destFolder}", overwrite:true)
		delete(file:"${tmpZip}")
	}
}

def downloadAndCopy(builder,currentFile,destFolder) {
	builder.sequential { 
		echo(message:"Getting ${currentFile} ...")
		get(src:"${currentFile}", dest:"${destFolder}", skipexisting:false)
	}
}

def upload(builder,currArgs,drRoot) {

	if (currArgs.length < 3) {
		return printUsage()
	}		
	def objType=currArgs[1]

	def currentFile= currArgs[2]
	
	switch (objType) {
		case ["module"]:
			downloadUnzipAndCopy(builder,currentFile,"${drRoot}/sites/all/modules")
			break
		case ["theme"]:	
			downloadUnzipAndCopy(builder,currentFile,"${drRoot}/sites/all/themes")
			break
		case ["file"]:	
			if (currArgs.length < 4) {
				return printUsage()
			}
			def relativePath=currArgs[3]
			if ( !relativePath.endsWith("/")) {
				relativePath += "/"
			}
			downloadAndCopy(builder,currentFile,"${drRoot}/${relativePath}")					
			break				
		default: throw new Exception("Support for ${objType} command is not implemented")
	}
}


builder = new AntBuilder()

def currCommandName=args[0]

def currOsName="unix"
drRoot=context.attributes.thisInstance["docRoot"]

switch (currCommandName) {
	case ["upload"]:	
		upload(builder,args,drRoot)
		break			
	default: throw new Exception("Support for ${currCommandName} command is not implemented")
}




println "drupal_commands.groovy: End"
