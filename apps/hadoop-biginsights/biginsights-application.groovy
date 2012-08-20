application {
	name="biginsights"
	
	service {
		name = "master"		
		dependsOn = ["data"]
	}		
	service {
		name = "data"
	}
	service {
		name = "dataOnDemand"
		dependsOn = ["master"]
	}
}
