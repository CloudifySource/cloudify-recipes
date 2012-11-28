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
service {
	name "master"
	icon "biginsights.png"
	numInstances 1
	
	compute {
		template "MASTER"
	}
		
	lifecycle {
		install "master_install.groovy"
		preStart "master_start.groovy" 		
		preStop "master_stop.groovy"
		startDetectionTimeoutSecs 2400	
		startDetection {
			if((new File(context.serviceDirectory + "/installationRunning")).exists())
			{
				return false;
			}
			println ":master-service.groovy: start detection: start detection checking port";						
			ServiceUtils.isPortOccupied(nameNodePort)
		}
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.eq=org.apache.hadoop.hdfs.server.namenode.NameNode")
			println ":master-service.groovy: current PIDs: ${myPids}"
			return myPids
        }		
        
		monitors {
	
			def nameNodeJmxBeans = [
			"Total Files": ["Hadoop:name=FSNamesystemMetrics,service=NameNode", "FilesTotal"],
			"Total Blocks": ["Hadoop:name=FSNamesystemMetrics,service=NameNode", "BlocksTotal"],
			"Capacity Used (GB)": ["Hadoop:name=FSNamesystemMetrics,service=NameNode", "CapacityUsedGB"],
			"Blocks with corrupt replicas": ["Hadoop:name=FSNamesystemMetrics,service=NameNode", "CorruptBlocks"],
			"Storage capacity utilization": ["Hadoop:name=NameNodeInfo,service=NameNode", "PercentUsed"],
			"Number of active metrics sources": ["Hadoop:name=MetricsSystem,service=NameNode,sub=Stats", "num_sources"],
			"Number of active metrics sinks": ["Hadoop:name=MetricsSystem,service=NameNode,sub=Stats", "num_sinks"],
			"Number of ops for snapshot stats": ["Hadoop:name=MetricsSystem,service=NameNode,sub=Stats", "snapshot_num_ops"],
			"Average time for snapshot stats": ["Hadoop:name=MetricsSystem,service=NameNode,sub=Stats", "snapshot_avg_time"],
			"Number of ops for publishing stats": ["Hadoop:name=MetricsSystem,service=NameNode,sub=Stats", "publish_num_ops"],
			"Average time for publishing stats": ["Hadoop:name=MetricsSystem,service=NameNode,sub=Stats", "publish_avg_time"],
			"Dropped updates by all sinks": ["Hadoop:name=MetricsSystem,service=NameNode,sub=Stats", "dropped_pub_all"],
			]
	
			return JmxMonitors.getJmxMetrics("127.0.0.1",nameNodeJmxPort,nameNodeJmxBeans)
		}        
		stopDetection {
		   	if(!(ServiceUtils.isPortOccupied(nameNodePort)))
		   	{
				if(!((context.serviceDirectory + "/installationRunning").exists()))
					return true;
		   	}
			return false;
		}
		details {
			def currPublicIP
			currPublicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			def bigInsightsURL	= "http://${currPublicIP}:8080"///BigInsights/console/NodeAdministration.jsp"

				return [
					"BigInsights URL":"<a href=\"${bigInsightsURL}\" target=\"_blank\"><img height=70 width=70 src='https://www.ibm.com/developerworks/mydeveloperworks/wikis/form/anonymous/api/library/77eb08fb-0fa9-4195-bad9-a905a1b2d461/document/8051ab37-10c0-41ca-92ae-888ad7cda61e/attachment/8142cf29-67d2-4035-8f28-6c8d5cfd6745/media/biginsights logo.png'></a>"
				]
		}	        		
	}
		
	customCommands ([
		"addNode" : "master_addNode.groovy",
		"removeNode" : "master_removeNode.groovy",
		"rebalance"	 : "master_rebalance.groovy",
		"dfs"	 : "master_dfs.groovy",
		"dfsadmin"	 : "master_dfsadmin.groovy",
	])


	userInterface {

		metricGroups = ([
		metricGroup {
		
		name "FSNameSystem"
		
		metrics([
		"Total Files",
		"Total Blocks",
		"Capacity Used (GB)",
		"Blocks with corrupt replicas",
		])
		} ,
		metricGroup {
		
		name "NameNode Stats"
		
		metrics([
		"Number of active metrics sources",
		"Number of active metrics sinks",
		"Number of ops for snapshot stats",
		"Average time for snapshot stats",
		"Number of ops for publishing stats",
		"Average time for publishing stats",
		"Dropped updates by all sinks",
		"Storage capacity utilization",
		])
		} ,
		]
		)
		
		widgetGroups = ([
		widgetGroup {
		
		name "Total Files"
		widgets([
		balanceGauge{metric = "Total Files"},
		barLineChart {
		metric "Total Files"
		axisYUnit Unit.REGULAR
		}
		])
		} ,
		widgetGroup {
		
		name "Total Blocks"
		widgets([
		balanceGauge{metric = "Total Blocks"},
		barLineChart {
		metric "Total Blocks"
		axisYUnit Unit.REGULAR
		}
		])
		} ,
		widgetGroup {
		
		name "Capacity Used (GB)"
		widgets([
		balanceGauge{metric = "Capacity Used (GB)"},
		barLineChart {
		metric "Capacity Used (GB)"
		axisYUnit Unit.REGULAR
		}
		])
		} ,
		widgetGroup {
		
		name "Blocks with corrupt replicas"
		widgets([
		balanceGauge{metric = "Blocks with corrupt replicas"},
		barLineChart {
		metric "Blocks with corrupt replicas"
		axisYUnit Unit.REGULAR
		}
		])
		} ,
		widgetGroup {
		
		name "Storage capacity utilization"
		widgets([
		balanceGauge{metric = "Storage capacity utilization"},
		barLineChart {
		metric "Storage capacity utilization"
		axisYUnit Unit.PERCENTAGE
		}
		])
		} ,
		]
		)
		}
    
    network {
		println ":master-service.groovy: nameNode Http port: ${nameNodePort}"
        port = nameNodePort
        protocolDescription ="NameNode"
    }
		
}
