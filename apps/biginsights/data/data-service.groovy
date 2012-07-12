service {
	name "data"
	icon "biginsights.png"
//	type "WEB_SERVER"
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 1
	
	compute {
		template "DATA"
	}
		
	lifecycle {
		install "data_install.groovy"
		start "data_start.groovy" 		
		preStop "data_stop.sh"
		startDetection {
			ServiceUtils.isPortOccupied(22)
		}		
	}
		
        	
}
