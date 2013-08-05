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
			The following custom command enables users to install an elasticsearch plugin 
			Usage :  
			  invoke elasticsearch plugin username/pluginName [relative_url]
			
			Example: 			
			   1. In order to install the bigdesk plugin (https://github.com/lukas-vlcek/bigdesk), 
			      you need to invoke the following command:
				invoke elasticsearch plugin lukas-vlcek/bigdesk _plugin/bigdesk
			  If the plugin is browsable, then once the plugin is installed, you can access it via :  ESINSTANCE_HOSTADDRESS:ES_PORT:/_plugin/bigdesk
			   2. In order to install the elasticsearch-head plugin (https://github.com/mobz/elasticsearch-head), 
			      you need to invoke the following command:
				invoke elasticsearch plugin mobz/elasticsearch-head _plugin/head/
			  If the plugin is browsable, then once the plugin is installed, you can access it via :  ESINSTANCE_HOSTADDRESS:ES_PORT:/_plugin/head/
			  
		*/	


context = ServiceContextFactory.getServiceContext()


def instanceID = context.instanceId
println "elasticsearch_plugin.groovy: instanceID is ${instanceID}"

def currHostAddress =  context.getPublicAddress()
println "elasticsearch_plugin.groovy: host address is ${currHostAddress}"

httpPort = context.attributes.thisInstance["httpPort"]
println "elasticsearch_rest.groovy: current instance's port is ${httpPort}"

def installationFolder = context.attributes.thisInstance["installationFolder"]
println "elasticsearch_plugin.groovy: installationFolder is ${installationFolder}"

def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
println "elasticsearch_plugin.groovy: scriptsFolder folder is ${scriptsFolder}"


/* Rest method : POST, GET or PUT */
if ( args.length == 0 ) {
	println "elasticsearch_plugin.groovy: Usage : invoke elasticsearch plugin pluginName [relative_url]"
	return 
}
def pluginName = args[0]
println "elasticsearch_plugin.groovy: pluginName is ${pluginName}"


builder = new AntBuilder()

def installPlugin = "installPlugin.sh"
builder.sequential {
	echo(message: "elasticsearch_plugin.groovy: Running ${scriptsFolder}/${installPlugin}")
	exec(executable: "${scriptsFolder}/${installPlugin}",failonerror: "false") {		
		arg(value:installationFolder)		
		arg(value:pluginName)		
	}
}

if ( args.length > 1 ) {	
	println "${pluginName} is now available in ${currHostAddress}:${httpPort}/" + args[1]
	return "${pluginName} is now available in ${currHostAddress}:${httpPort}/" + args[1]	
}	

println "elasticsearch_plugin.groovy: End of rest"