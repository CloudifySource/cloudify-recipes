import static JmxMonitors.*

service {
	extend "../../../services/tomcat"		
	elastic false
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 1
	
	def portIncrement =  context.isLocalCloud() ? context.getInstanceId()-1 : 0		
	
	def currJmxPort = jmxPort + portIncrement
	def currHttpPort = port + portIncrement
	def currAjpPort = ajpPort + portIncrement
			
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
	}	
}