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
@GrabResolver(name='Codehaus', root='http://repository.codehaus.org')
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6')

import groovyx.net.http.*

class ElasticsearchMonitors {


	def static getMetrics(currPublicIP,currPrivateIP,currentHttpPort) { 
		try { 	
			println "In getMetrics"			
							
			def restUrl="http://${currPublicIP}:${currentHttpPort}"
				
			def metrics=[:]
			def http = new HTTPBuilder( restUrl )
			def restQuery = "_nodes/${currPrivateIP}/stats"
			println restUrl+ "/" + restQuery
			http.get( path: restQuery, query: [all:'true']) { resp, json ->

				def currNodeName
				if  ( resp.status == 200 ) { 
					def currNodes = json.nodes	
					currNodes.each {
						currNodeName = it.key 		
					}	

					def currNode = json.nodes."${currNodeName}"
					def usedMem = Math.floor(currNode.os.mem.actual_used_in_bytes/1024/1024)
					println "elasticsearch_monitors: Used Memory (MB):" + usedMem
					def fs = currNode.fs		
					def diskReads = fs.data.disk_reads[0]				
					println "elasticsearch_monitors: Disk Reads " + diskReads
					def diskWrites = fs.data.disk_writes[0]
					println "elasticsearch_monitors: Disk Writes " + diskWrites
										
					metrics.put("ES Used Memory",usedMem)
					metrics.put("ES Disk Reads",diskReads)
					metrics.put("ES Disk Writes",diskWrites)			
					return metrics 
				}					
			}
		}
		catch(Throwable e) {
			println "elasticsearch_monitors: throwable is " + e
			return [
				"ES Used Memory" : 0 , 
				"ES Disk Reads" : 0 , 
				"ES Disk Writes" : 0 
			]
		}
	}
}