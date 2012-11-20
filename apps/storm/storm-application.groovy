application {
	name="storm"
	
	service {
		name = "zookeeper"		
	}
	
	service {
		name = "storm-nimbus"
		dependsOn = ["zookeeper"]		
	}

	service {
		name = "storm-supervisor"
		dependsOn = ["storm-nimbus"]
	}
}