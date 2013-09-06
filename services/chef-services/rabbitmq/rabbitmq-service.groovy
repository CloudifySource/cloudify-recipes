service {
	extend "../../../services/chef"
	name "rabbitmq"
	icon "rabbitmq.png"
	type "MESSAGE_BUS"
	
	elastic true
	numInstances 2
	minAllowedInstances 2
	maxAllowedInstances 2
	
	compute {
		template "SMALL_UBUNTU"
	}
	
	lifecycle{
		
		preInstall "rabbitmq_preInstall.groovy"
		
		postInstall "rabbitmq_postInstall.groovy"
		
		preStart {
			def config=new ConfigSlurper().parse(new File("${context.serviceDirectory}/rabbitmq-service.properties").toURL())
			
			def myInstanceID=context.getInstanceId()
			
			if (myInstanceID != 1){
				println "preStart: My Instance ID is: ${myInstanceID}. firstInstanceReady: ${context.attributes.thisService['firstInstanceReady']}."
				while (!context.attributes.thisService["firstInstanceReady"]){
					println "preStart: Wait for the first instance to become ready ... "
					Thread.sleep(5000)
				}
			} 
			
			boolean isDisk = false
			def hostname = context.attributes.thisInstance["hostname"]
			def rabbitNodeName = "rabbit"
			
			def rabbitmqInstances = context.attributes.thisService.instances;
			int diskNodesCount = 0
			
			StringBuilder diskNodeNamesBuilder = new StringBuilder()
			
			for (i in rabbitmqInstances){
				if (i.isDisk && i.instanceId != myInstanceID){
					diskNodesCount ++
					diskNodeNamesBuilder.append("rabbit@${i.hostname} ")
				}
			}
			
			def numberOfConfiguredDiskNodes
			
			if (config.numberOfDiskNodes != null){
				numberOfConfiguredDiskNodes = config.numberOfDiskNodes
			} else {
				numberOfConfiguredDiskNodes = 2
			}
			
			println "Disk_Nodes_Count: " + diskNodesCount
			println "numberOfConfiguredDiskNodes: " + numberOfConfiguredDiskNodes
			
			if (diskNodesCount < numberOfConfiguredDiskNodes){
				isDisk = true
			}
			
			println "Set isDisk for current node to: " + isDisk
			context.attributes.thisInstance["isDisk"] = isDisk
			
			if (isDisk){
				diskNodeNamesBuilder.append("rabbit@${hostname} ")
			}
			
			def diskNodeNames = diskNodeNamesBuilder.toString().trim()
			
			def runParams = context.attributes.thisInstance["runParams"]
			
			runParams["rabbitmq"]["cluster"] = true
			runParams["rabbitmq"]["cluster_disk_nodes"] = diskNodeNames.split(" ")
			
			context.attributes.thisInstance["runParams"] = runParams
			
		}
		
		postStart "rabbitmq_postStart.groovy"
		
		startDetectionTimeoutSecs 9000
		startDetection {
			if(context.attributes.thisInstance["port"] != null){
				return ServiceUtils.isPortOccupied(context.attributes.thisInstance["port"])
			} else {
				println "Port is not assigned yet (i.e., null)."
				return false
			}
		}
		
		stopDetection {
			boolean hasStoped = !ServiceUtils.isPortOccupied(context.attributes.thisInstance["port"])
			return hasStoped
		}
		
		preStop "rabbitmq_preStop.groovy"
	}
	
	customCommands ([
		/*
			This custom command adds a host file entry to the hosts file of the machine running this rabbitmq node. 
			It enables a new rabbitmq instance to inform its hostname and ip address to all other existing instances 
			and add an host file entry to their hosts file, so that the new node's hostname can be resovled on all 
			instances in the cluster.
			Usage :  invoke rabbitmq addHostFileEntry host_file_entry
			Example: invoke rabbitmq addHostFileEntry "172.18.48.156 rabbit10"
		*/
	
		"addHostFileEntry" : "add_hostfile_entry.groovy"
	])
	
	userInterface {
		
		metricGroups = ([
			metricGroup {
		
				name "process"
		
				metrics([
					"Total Process Cpu Time",
					"Process Cpu Kernel Time",
					"Total Process Residential Memory",
					"Total Num of Page Faults"
				])
			}
		]
		)
		
		widgetGroups = ([
			widgetGroup {
				name "Total Process Cpu Time"
				widgets([
					balanceGauge{metric = "Total Process Cpu Time"},
					barLineChart {
						metric "Total Process Cpu Time"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Process Cpu Kernel Time"
				widgets([
					balanceGauge{metric = "Process Cpu Kernel Time"},
					barLineChart {
						metric "Process Cpu Kernel Time"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Total Process Residential Memory"
				widgets([
					balanceGauge{metric = "Total Process Residential Memory"},
					barLineChart {
						metric "Total Process Residential Memory"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Total Num of Page Faults"
				widgets([
					balanceGauge{metric = "Total Num of Page Faults"},
					barLineChart {
						metric "Total Num of Page Faults"
						axisYUnit Unit.REGULAR
					}
				])
			}
		]
		)
	}
	
	scaleCooldownInSeconds 20
	samplingPeriodInSeconds 1
		
	scalingRules ([
		scalingRule {

			serviceStatistics {
				metric "Total Process Cpu Time"
				timeStatistics Statistics.averageCpuPercentage
				instancesStatistics Statistics.maximum
				movingTimeRangeInSeconds 20
			}

			highThreshold {
				value 40
				instancesIncrease 1
			}

			lowThreshold {
				value 25
				instancesDecrease 1
			}
		}
	])
	
}
