service {
	name "master"
	icon "biginsights.png"
	numInstances 1
	
	compute {
		template "MASTER"
	}
		
	lifecycle {
		install "master_install.groovy"
		start "master_start.groovy" 		
		preStop "master_stop.sh"
		startDetectionTimeoutSecs 2400	
		startDetection {
			ServiceUtils.isPortOccupied(22)
		}
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.eq=org.apache.hadoop.hdfs.server.namenode.NameNode")
			println ":master-service.groovy: current PIDs: ${myPids}"
			return myPids
        }		

		details {
			def currPublicIP
			currPublicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			def bigInsightsURL	= "http://${currPublicIP}:8080/BigInsights/console/NodeAdministration.jsp"

				return [
					"BigInsights URL":"<a href=\"${bigInsightsURL}\" target=\"_blank\"><img height=70 width=70 src='https://www.ibm.com/developerworks/mydeveloperworks/wikis/form/anonymous/api/library/77eb08fb-0fa9-4195-bad9-a905a1b2d461/document/8051ab37-10c0-41ca-92ae-888ad7cda61e/attachment/8142cf29-67d2-4035-8f28-6c8d5cfd6745/media/biginsights logo.png'></a>"
				]
		}	        		
	}
		
    
    network {
        port = 8080
        protocolDescription ="HTTP"
    }
		
}
