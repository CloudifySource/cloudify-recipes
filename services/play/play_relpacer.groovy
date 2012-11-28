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

import org.cloudifysource.dsl.context.ServiceContextFactory



/* 
	This file enables users to replace a string in a file (relative to play home folder
	Usage : invoke play replace all|first origString newString relativePath
		
	Examples: 
	    1. The following replaces all the occurrences of DEBUG with ERROR 
		   in PLAY_HOME_FOLDER/framework/src/play/src/main/resources/reference.conf : 
			invoke play replace all "DEBUG" "ERROR" /framework/src/play/src/main/resources/reference.conf
			
	    2. The following replaces the 1st occurrence of DEBUG with ERROR 
		   in PLAY_HOME_FOLDER/framework/src/play/src/main/resources/reference.conf : 
			invoke play replace first "DEBUG" "ERROR" /framework/src/play/src/main/resources/reference.conf			
*/	

config=new ConfigSlurper().parse(new File('play-service.properties').toURL())
context = ServiceContextFactory.getServiceContext()

playRootFolder=context.serviceDirectory + "/${config.name}"
println "play_relpacer.groovy: playRootFolder is ${playRootFolder}"

def msgText 
if (args.length < 4) {
	msgText = "play_relpacer.groovy: replace custom command error : Missing parameters\nUsage: \n " +
	"1. The following replaces all the occurrences of DEBUG with ERROR \n" +
		   "in PLAY_HOME_FOLDER/framework/src/play/src/main/resources/reference.conf :  \n" + 
			"invoke play replace all \"DEBUG\" \"ERROR\" /framework/src/play/src/main/resources/reference.conf \n\n" + 			
	    "2. The following replaces the 1st occurrence of DEBUG with ERROR \n" + 
		 "  in PLAY_HOME_FOLDER/framework/src/play/src/main/resources/reference.conf :  \n" + 
		 " invoke play replace first \"DEBUG\" \"ERROR\" /framework/src/play/src/main/resources/reference.conf	\n" 
	
	println msgText
	return msgText
	System.exit(-1)
}


def firstOrAll = args[0]
def origString = args[1]
def newString =  args[2] 
def relativePath = args[3] 


def fullPath="${playRootFolder}/${relativePath}"
def file2bReplaced = new File(fullPath)
text = file2bReplaced.text


if ( firstOrAll == "first" ) {
	msgText = "play_relpacer.groovy: Replacing the 1st occurrence of ${origString} with ${newString} in ${fullPath}..."
	println msgText	
	text = text.replaceFirst("${origString}", "${newString}")
}
else {
	msgText = "play_relpacer.groovy: Replacing all the occurrences of ${origString} with ${newString} in ${fullPath}..."
	text = text.replaceAll("${origString}", "${newString}")
}
file2bReplaced.text = text



println "play_relpacer.groovy: End"
	
return msgText + "\nplay_relpacer.groovy: End"
 
