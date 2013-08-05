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

/* 
			// This file enables users to invoke rest commands on elasticsearch 
			Usage :  invoke elasticsearch rest restMethod command 
			
			
			Examples: 
			   1. In order to invoke the following elasticsearch rest command :
					curl -XGET http://IP_ADDRESS:9200/twitter/tweet/4
			      you need to invoke the following command from Cloudify CLI : 
			   invoke elasticsearch rest GET twitter/tweet/4

			   2. In order to invoke the following elasticsearch rest command :
					curl -XPUT http://IP_ADDRESS:9200/twitter/tweet/4 -d '{ "user": "Mike", "message": "Good Job" }'
			      you need to invoke the following command from Cloudify CLI (use # as a field delimiter) : 
			   invoke elasticsearch rest PUT twitter/tweet/4 "user:Mike#message:Good"
			   
			   3. In order to invoke the following elasticsearch rest command :
					curl -XGET 'http://IP_ADDRESS:9200/_cluster/health?pretty=true'
			      you need to invoke the following command from Cloudify CLI :
			   invoke elasticsearch rest GET _cluster/health?pretty=true
			
			
		*/


def setCommandsArgs(restArgs) {
		    
	def commandArgs = ""
	def restArgsLen=restArgs.length()
	if ( restArgsLen > 0  ) { 		
		def rawArgs = restArgs.split("#")
		rawArgs.each{currPairRaw->
			pairArray = currPairRaw.split(":")	
			if ( pairArray.length > 1 ) { 
				commandArgs += "\""  + pairArray[0] + "\":"  + "\"" + pairArray[1] + "\","		
			}
			else {
				commandArgs += "\""  + pairArray[0] + "\","
			}
		}

		commandArgsLen=commandArgs.length()
		if ( commandArgsLen > 0 && commandArgs.endsWith(",")) {	
			commandArgs = commandArgs.substring(0,commandArgsLen-1)
			commandArgs = "{" + commandArgs + "}";
		}
	}
	println "elasticsearch_rest.groovy.setCommandsArgs: commandArgs are ${commandArgs}"
	return commandArgs
}


context = ServiceContextFactory.getServiceContext()


def instanceID = context.instanceId
println "elasticsearch_rest.groovy: instanceID is ${instanceID}"

if ( instanceID != 1 ) {
	return
}

println "elasticsearch_rest.groovy: host address is " + context.attributes.thisInstance["myHostAddress"]

httpPort = context.attributes.thisInstance["httpPort"]
println "elasticsearch_rest.groovy: current instance's httpPort is ${httpPort}"

def installationFolder = context.attributes.thisInstance["installationFolder"]
println "elasticsearch_rest.groovy: installationFolder is ${installationFolder}"

def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
println "elasticsearch_rest.groovy: scriptsFolder folder is ${scriptsFolder}"


/* Rest method : POST, GET or PUT */
def restMethod = args[0]
println "elasticsearch_rest.groovy: restMethod is ${restMethod}"

def currCommand = args[1]
println "elasticsearch_rest.groovy: command is ${currCommand}"

def commandArgs=""
if (args.length > 2) {
	def restArgs = "" 
	for ( i=2; i<args.length; i++){
		restArgs += args[i] + " " 
	}
	restArgs = restArgs.trim()
	println "elasticsearch_rest.groovy: rest Args are ${restArgs}"
	commandArgs=setCommandsArgs(restArgs)
	println "elasticsearch_rest.groovy: commandArgs are ${commandArgs}"
}	


builder = new AntBuilder()

def invokeRest = "invokeRest.sh"
builder.sequential {
	echo(message: "elasticsearch_rest.groovy: Running ${scriptsFolder}/${invokeRest}")
	exec(executable: "${scriptsFolder}/${invokeRest}",failonerror: "true") {		
		arg(value:installationFolder)		
		arg(value:"localhost")		
		arg(value:httpPort)		
		arg(value:restMethod)		
		arg(value:currCommand)		
		arg(value:commandArgs)		
	}
}

println "elasticsearch_rest.groovy: End of rest"