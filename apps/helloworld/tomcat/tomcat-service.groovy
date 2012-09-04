import static JmxMonitors.*

service {
	name "tomcat"
	icon "tomcat.gif"
	type "APP_SERVER"
	
    elastic false
	numInstances 1
		
	def portIncrement =  context.isLocalCloud() ? context.getInstanceId()-1 : 0		
	
	def currJmxPort = jmxPort + portIncrement
	def currHttpPort = port + portIncrement
	def currAjpPort = ajpPort + portIncrement
		
	compute {
		template "SMALL_LINUX"
	}

	lifecycle {
	
	
		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP =InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
			def tomcatURL	= "http://${currPublicIP}:${currHttpPort}"	
						
			def applicationURL = "${tomcatURL}/${ctxPath}"
			println "tomcat-service.groovy: applicationURL is ${applicationURL}"
		
            return [
                "Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
            ]
		}	

		monitors {
										
			def metricNamesToMBeansNames = [
				"Current Http Threads Busy": ["Catalina:type=ThreadPool,name=\"http-bio-${currHttpPort}\"", "currentThreadsBusy"],				
				"Current Http Thread Count": ["Catalina:type=ThreadPool,name=\"http-bio-${currHttpPort}\"", "currentThreadCount"],				
				"Backlog": ["Catalina:type=ProtocolHandler,port=${currHttpPort}", "backlog"],				
				"Total Requests Count": ["Catalina:type=GlobalRequestProcessor,name=\"http-bio-${currHttpPort}\"", "requestCount"],				
				"Active Sessions": ["Catalina:type=Manager,context=/${ctxPath},host=localhost", "activeSessions"],
			]
			
			return getJmxMetrics("127.0.0.1",currJmxPort,metricNamesToMBeansNames)										
    	}			
	
	
	
		install "tomcat_install.groovy"
		start "tomcat_start.groovy"		
		preStop "tomcat_stop.groovy"
		startDetectionTimeoutSecs 240
		startDetection {
			println "tomcat-service.groovy(startDetection): arePortsFree http=${currHttpPort} ajp=${currAjpPort} ..."
			!ServiceUtils.arePortsFree([currHttpPort, currAjpPort] )
		}
		
		
		def instanceID = context.instanceId
		
				
		
	}
	
	userInterface {

		metricGroups = ([
			metricGroup {

				name "process"

				metrics([
				    "Total Process Cpu Time",
					"Process Cpu Usage",
					"Total Process Virtual Memory",
					"Num Of Active Threads"
				])
			} ,
			metricGroup {

				name "http"

				metrics([
					"Current Http Threads Busy",
					"Current Http Thread Count",
					"Backlog",
					"Total Requests Count"
				])
			} ,

		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Process Cpu Usage"
				widgets ([
					balanceGauge{metric = "Process Cpu Usage"},
					barLineChart{
						metric "Process Cpu Usage"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name "Total Process Virtual Memory"
				widgets([
					balanceGauge{metric = "Total Process Virtual Memory"},
					barLineChart {
						metric "Total Process Virtual Memory"
						axisYUnit Unit.MEMORY
					}
				])
			},
			widgetGroup {
				name "Num Of Active Threads"
				widgets ([
					balanceGauge{metric = "Num Of Active Threads"},
					barLineChart{
						metric "Num Of Active Threads"
						axisYUnit Unit.REGULAR
					}
				])
			}     ,
			widgetGroup {

				name "Current Http Threads Busy"
				widgets([
					balanceGauge{metric = "Current Http Threads Busy"},
					barLineChart {
						metric "Current Http Threads Busy"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {

				name "Current Http Thread Count"
				widgets([
					balanceGauge{metric = "Current Http Thread Count"},
					barLineChart {
						metric "Current Http Thread Count"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {

				name "Request Backlog"
				widgets([
					balanceGauge{metric = "Backlog"},
					barLineChart {
						metric "Backlog"
						axisYUnit Unit.REGULAR
					}
				])
			}  ,
			widgetGroup {
				name "Active Sessions"
				widgets([
					balanceGauge{metric = "Active Sessions"},
					barLineChart {
						metric "Active Sessions"
						axisYUnit Unit.REGULAR
					}
				])
			},
			widgetGroup {
				name "Total Requests Count"
				widgets([
					balanceGauge{metric = "Total Requests Count"},
					barLineChart {
						metric "Total Requests Count"
						axisYUnit Unit.REGULAR
					}
				])
			}
			,
			widgetGroup {
				name "Total Process Cpu Time"
				widgets([
					balanceGauge{metric = "Total Process Cpu Time"},
					barLineChart {
						metric "Total Process Cpu Time"
						axisYUnit Unit.REGULAR
					}
				])
			}
		]
		)
	}
	
	network {
        port = currHttpPort
        protocolDescription ="HTTP"
    }
		
}