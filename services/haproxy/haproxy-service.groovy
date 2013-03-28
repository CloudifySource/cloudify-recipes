service {
	
	name "haproxy"
	icon "haproxy.png"
	type "LOAD_BALANCER"
	
	elastic false
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 1
	
	compute {
		template "MEDIUM_LINUX"
	}
	
	
	lifecycle{
		details {
			
			def currPublicIP
			
			if (context.isLocalCloud()) {
				currPublicIP = InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP = System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
			
			def config=new ConfigSlurper().parse(new File("${context.serviceDirectory}/haproxy-service.properties").toURL())
			
			def statisticPort = config.statisticPort
			def frontEndPort = config.frontEndPort
			
			def statisticURL = "http://${currPublicIP}:${statisticPort}/stats"
			def frontEndIPandPort = "${currPublicIP}:${frontEndPort}"
			
			println "haproxy-service.groovy: statisticsURL is ${statisticURL}"
			return [
				"Statistics URL": "<a href=\"${statisticURL}\" target=\"_blank\">${statisticURL}</a>",
				"Front End IP and Port": "${frontEndIPandPort}"
			]
		}
		
		install "haproxy_install.groovy"
		
		postInstall "haproxy_postInstall.groovy"
		
		start "haproxy_start.groovy"
	
		startDetectionTimeoutSecs 9000

		startDetection {
			println "haproxy-service.groovy. Start detection. Checking port ${statisticPort}"
			boolean hasStarted = ServiceUtils.isPortOccupied(statisticPort)
			
			if (hasStarted) {
				println "haproxy-service.groovy: service started"
			} else {
				println "haproxy-service.groovy: service not started yet"
			}
		
			return hasStarted
			
		}
		
		stopDetection {
			return !ServiceUtils.isPortOccupied(statisticPort)
		}
		
		locator {
			return ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=haproxy")
		}
		
		preStop "haproxy_preStop.groovy"
	}
	
	customCommands ([
		/*
			This custom command enables a new back end server (e.g., a rabbitmq instance) to to add itself to the
			configuration of haproxy, so that haproxy can distribute load to it. The command contains the logic to
			reload the new configuration so that the configuration change can take effect immediately.
			 
			Usage :  invoke haproxy addNode IP_of_New_Node Port_of_New_Node
			Example: invoke haproxy addNode myrabbit 172.18.48.110 5672
		*/
		"addNode" : "haproxy_addNode.groovy",
	
		/*
			This custom command adds a tomcat node. It adds a front end router and backend on addition of first node. 
			Adds extra node to backend if it is an additional node.
			
			Usage :  invoke haproxy addNode hostname:port/webappname webappname instanceId
			Example: invoke haproxy addNode localhost:8080/trading-interface trading-interface 4
			
		*/
		
		"addTomcatNode" : "haproxy_addTomcatNode.groovy",	
		/*
		 This custom command enables a back end server (e.g., a rabbitmq instance) to remove itself from the load
		 balancer when the server is going to stop, so that haproxy will not distribute further load to it. The
		 command contains logic to reload the new configuration so that haproxy will stop to distribute load to this
		 particular server immediately.
		 
		 Usage :  invoke haproxy removeNode IP_of_New_Node Port_of_New_Node
		 Example: invoke haproxy removeNode 172.18.48.156 5672
	 */
		"removeNode" : "haproxy_removeNode.groovy",
	
		/*
			Removes Tomcat node.
			
			Usage :  invoke haproxy removeTomcatNode webappName instanceId
		    Example: invoke haproxy removeTomcatNode trading-interface 3
		*/
		"removeTomcatNode" : "haproxy_removeTomcatNode.groovy"
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
	
}