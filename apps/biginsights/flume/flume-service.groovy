service {
	name "flume"
//	type "WEB_SERVER"
	numInstances 1
	
	compute {
		template "FLUME"
	}
		
	lifecycle {
		install "flume_install.groovy"
		start "flume_start.groovy" 		
		preStop "flume_stop.sh"
		startDetection {
			ServiceUtils.isPortOccupied(22)
		}		
	}
		
        	
}
