import java.util.concurrent.TimeUnit;

service {
	name "master"
	icon "biginsights.png"
	numInstances 1
	
	compute {
		template "SMALL_LINUX"
	}
		
	lifecycle {
                def fulladdress= context.getPrivateAddress()
                def privateIP = fulladdress.split("/")[0]

		install "master_install.groovy"
		preStart "master_start.groovy" 		
/*		postStart ******Use this to add additional BigInsights services to the master node****
		{
			println "dataOnDemand-service.groovy: master Post-start ..."
			def instanceID = context.instanceId	
			println "master-service.groovy: privateIP is ${privateIP} ..."
			def masterService = context.waitForService("master", 180, TimeUnit.SECONDS)			
	        masterService.invoke("addNode", privateIP as String, "hadoop", instanceID as String)
			masterService.invoke("addNode", privateIP as String, "hbase", instanceID as String)			
			println "master-service.groovy: master Post-start ended"						
		}*/
		preStop "master_stop.groovy"
		startDetectionTimeoutSecs 2400	
		startDetection {
			if((new File(context.serviceDirectory + "/installationRunning")).exists())
			{
				return false;
			}
			println ":master-service.groovy: start detection: start detection checking port " + nameNodePort;						
			ServiceUtils.isPortOccupied(privateIP, nameNodePort)
		}
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.ew=${nameNodeJmxPort}")
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
	
			return JmxMonitors.getJmxMetrics("127.0.0.1",nameNodeJmxPort,jmxCredsPath,nameNodeJmxBeans)
		}        
		stopDetection {
		   	if(!(ServiceUtils.isPortOccupied(privateIP, nameNodePort)))
		   	{
				if(!((new File(context.serviceDirectory + "/installationRunning")).exists()))
					return true;
		   	}
			return false;
		}
		details {
            def fulladdress2= context.getPublicAddress()
        	def privateIP2 = fulladdress2.split("/")[0]
			def bigInsightsURL	= "http://${privateIP2}:8080"///BigInsights/console/NodeAdministration.jsp"

				return [
					"HADOOP_HOME":"${HADOOP_HOME}","BigInsights URL":"<a href=\"${bigInsightsURL}\" target=\"_blank\"><img height=70 width=70 src='https://www.ibm.com/developerworks/mydeveloperworks/wikis/form/anonymous/api/library/77eb08fb-0fa9-4195-bad9-a905a1b2d461/document/8051ab37-10c0-41ca-92ae-888ad7cda61e/attachment/8142cf29-67d2-4035-8f28-6c8d5cfd6745/media/biginsights logo.png'></a>"
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
