import java.util.concurrent.TimeUnit;

service {
	name "nodejs"
	icon "nodejs.png"
	type "APP_SERVER"
	
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 1
	
	def instanceId = context.instanceId
	
	compute {
		template "SMALL_LINUX"
	}
	lifecycle {
	
		details {
			def currPublicIP = context.publicAddress			
			def applicationURL = "http://${currPublicIP}:${demoApplicationPort}"
			println "nodejs-service.groovy: applicationURL is ${applicationURL}"
			
			return [
				"Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
			]
		}	
	
	
		install "nodejs_install.groovy"
		start   "nodejs_start.groovy"
		
	   startDetectionTimeoutSecs 800
		startDetection {                        
				ServiceUtils.isPortOccupied(demoApplicationPort)
		}        
		
		locator {                                           
               def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=node")
               println ":current PIDs: ${myPids}"
               return myPids
		}             
	}
}