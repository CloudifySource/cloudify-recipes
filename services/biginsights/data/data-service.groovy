service {
	name "data"
	icon "biginsights.png"
	numInstances 1
	
	compute {
		template "SMALL_LINUX"
	}
		
	lifecycle {
		install "data_install.groovy"
		start "data_start.groovy" 		
		preStop "data_stop.sh"
		startDetectionTimeoutSecs 3000	
		startDetection {
			ServiceUtils.isPortOccupied(22)
		}	
		locator {			
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.ew=${dataNodeJmxPort}")
			println ":data-service.groovy: current PIDs: ${myPids}"
			return myPids
        }					
	}
	customCommands ([                                                         
              "installationDone" : "data_installationDone.groovy"                
         ])    
    network {
        port = dataNodePort
        protocolDescription ="DataNode"
    }
        	
}
