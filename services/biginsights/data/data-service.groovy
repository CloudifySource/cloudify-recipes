service {
	name "data"
	icon "biginsights.png"
	numInstances 2
	minAllowedInstances 1
	maxAllowedInstances 3
	
	compute {
		template "DATA"
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
			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.eq=org.apache.hadoop.hdfs.server.datanode.DataNode")
			println ":data-service.groovy: current PIDs: ${myPids}"
			return myPids
        }					
	}
		
    network {
        port = dataNodePort
        protocolDescription ="DataNode"
    }
        	
}
