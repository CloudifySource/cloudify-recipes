application {
	name="xap"
	
	service {
		name = "mgt"	
	}
	service {
		name = "pu"
		dependsOn = ["mgt"]
	}

	service {
		name = "webui"
		dependsOn = ["mgt"]
	}	

}