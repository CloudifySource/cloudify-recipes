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

	/* The file enables users to invoke a play command line and up to 3 arguments.
		   Here are some examples : 
		    1. invoke play commandline compile
		    2. invoke play commandline clean
		    3. invoke play commandline clean-all
		    4. invoke play commandline test
		    5. invoke play commandline package
		    6. invoke play commandline update
		  */



def firstArg
def scndArg = ""
def thirdArg = ""

def msgText
		  
if (args.length < 1) {
	msgText = "play_commandline.groovy: This custom command enables users to invoke a play command line and up to 3 arguments \n" + 
		" Here are some examples : \n" + 
		    " 1. invoke play commandline compile \n" + 
		    " 2. invoke play commandline clean  \n" + 
		    " 3. invoke play commandline clean-all \n" + 
		    " 4. invoke play commandline test \n" + 
		    " 5. invoke play commandline package \n" + 
		    " 6. invoke play commandline update \n" 
	println msgText
	return msgText
	System.exit(-1)
}		  
		  
if (args.length >2 ) {
	thirdArg = args[2]
}
if (args.length >1 ) {
	scndArg = args[1]
}
firstArg = args[0]		  
	
def argLine="${firstArg} ${scndArg} ${thirdArg}"		  	

def config = new ConfigSlurper().parse(new File("play-service.properties").toURL())
serviceContext = ServiceContextFactory.getServiceContext()
playRootFolder=serviceContext.serviceDirectory + "/${config.name}"
applicationDir="${playRootFolder}/playApps/${config.applicationName}"

msgText = "play_cmd.groovy: Executing ${playRootFolder}/play ${argLine} in  ${applicationDir} ..."
println "${msgText}" 
new AntBuilder().sequential {
	echo(message:"${msgText}")
	exec(executable:"${playRootFolder}/play", dir:"${applicationDir}", osfamily:"unix") {       
		arg(line:"${argLine}")			
	}	
}
  
def endStr="play_cmd.groovy: End"  
println "${endStr}" 
msgText += "n\${endStr}"
return msgText
