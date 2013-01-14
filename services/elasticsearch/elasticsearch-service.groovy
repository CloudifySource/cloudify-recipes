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

import static ElasticsearchMonitors.*

service {
	
	name serviceName
	icon "elasticsearch.jpg"
	type "NOSQL_DB"
	elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 9

	compute {
		template "SMALL_LINUX"
	}
	
	def instanceID = context.instanceId
	def portIncrement =  context.isLocalCloud() ? instanceID-1 : 0			
	def currentHttpPort = httpPort + portIncrement
	
		
	lifecycle {
	
	
		details {
			def currPublicIP =context.getPublicAddress()
				
			def hostAndPort="http://${currPublicIP}:${currentHttpPort}"
			def applicationURL = "${hostAndPort}"
		
			def detailsMap = [
				"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>" , 
			]
													
			return detailsMap
		}	
		 
		monitors { 
			try {				
				def currentPublicIP = context.getPublicAddress()
				def currentPrivateIP = context.getPrivateAddress()
				def currentEsHttpPort = context.attributes.thisInstance["httpPort"]
				def mtr = getMetrics(currentPublicIP,currentPrivateIP,currentEsHttpPort)							
				return mtr;
			}
			catch(Throwable e) {
				println "elasticsearch-service: Throwable " +e 
			}
		}
				
		install "elasticsearch_install.groovy"
						
		start "elasticsearch_start.groovy"		
			
		startDetectionTimeoutSecs 800
		startDetection {			
			ServiceUtils.isPortOccupied(currentHttpPort)
		}	
		
		stopDetection {	
			!ServiceUtils.isPortOccupied(currentHttpPort)
		}		
		
		preStop "elasticsearch_stop.groovy"
			
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.ct=elasticsearch")
			println "elasticsearch-service.groovy: current PIDs: ${myPids}"
			return myPids
        }				
	}

	customCommands ([
		/* 
			This custom command enables users to invoke rest commands on elasticsearch 
			Usage :  invoke elasticsearch rest restMethod command 
			
			
			Examples: 
			   1. In order to invoke the following elasticsearch rest command :
					curl -XGET http://IP_ADDRESS:9200/twitter/tweet/4
			      you need to invoke the following command from Cloudify CLI : 
			        invoke elasticsearch rest GET twitter/tweet/4

			   2. In order to invoke the following elasticsearch rest command :
					curl -XPUT http://IP_ADDRESS:9200/twitter/tweet/4 -d '{ "user": "Mike", "message": "Good Job" }'
			      you need to invoke the following command from Cloudify CLI (use # as a field delimiter) : 
			        invoke elasticsearch rest PUT twitter/tweet/4 "user:Mike#message:Great"
			   
			   3. In order to invoke the following elasticsearch rest command :
					curl -XGET 'http://IP_ADDRESS:9200/_cluster/health?pretty=true'
			      you need to invoke the following command from Cloudify CLI :
			        invoke elasticsearch rest GET _cluster/health?pretty=true
					
		*/
	
		"rest" : "elasticsearch_rest.groovy" ,
		
		/*
			The following custom command enables users to install an elasticsearch plugin 
			Usage :  
			  invoke elasticsearch plugin username/pluginName [relative_url]
			
			Example: 			
			   1. In order to install the bigdesk plugin, you need to invoke the following command:
				invoke elasticsearch plugin lukas-vlcek/bigdesk _plugin/bigdesk
			  If the plugin is browsable, then once the plugin is installed, you can access it via :  ESINSTANCE_HOSTADDRESS:ES_PORT:/_plugin/bigdesk
			   2. In order to install the elasticsearch-head plugin (https://github.com/mobz/elasticsearch-head), 
			      you need to invoke the following command:
				invoke elasticsearch plugin mobz/elasticsearch-head _plugin/head/
			  If the plugin is browsable, then once the plugin is installed, you can access it via :  ESINSTANCE_HOSTADDRESS:ES_PORT:/_plugin/head/
			  
		*/		
		"plugin" : "elasticsearch_plugin.groovy" 
    ])		
	
	network {
		port currentHttpPort
		protocolDescription "HTTP"
	}
	
	userInterface {

		metricGroups = ([
			metricGroup {

				name "ES Stats"

				metrics([
				    "ES Used Memory",
					"ES Disk Reads",
					"ES Disk Writes"					
				])
			}
		])

		widgetGroups = ([
			widgetGroup {
				name "ES Used Memory"
				widgets ([
					balanceGauge{metric = "ES Used Memory"},
					barLineChart{
						metric "ES Used Memory"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "ES Disk Reads"
				widgets([
					balanceGauge{metric = "ES Disk Reads"},
					barLineChart {
						metric "ES Disk Reads"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "ES Disk Writes"
				widgets ([
					balanceGauge{metric = "ES Disk Writes"},
					barLineChart{
						metric "ES Disk Writes"
						axisYUnit Unit.REGULAR
					}
				])
			}   
		])
	}	
	
			
}
