service {
	name "hbase"
	icon "hbase.png"
//	type "WEB_SERVER"
	numInstances 1
	
	compute {
		template "HBASE"
	}
		
	lifecycle {
		install "hbase_install.groovy"
		start "hbase_start.groovy" 		
		preStop "hbase_stop.sh"
		startDetection {
			ServiceUtils.isPortOccupied(22)
		}		
	}
		
        	
}
