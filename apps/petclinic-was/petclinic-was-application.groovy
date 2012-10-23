application {
	name="petclinic"
	
	service {
		name = "mongod"		
	}
	
	service {
		name = "mongoConfig"		
	}
		
	service {
		name = "mongos"
		dependsOn = ["mongoConfig", "mongod"]
	}
	
	service {
		name = "websphere"
		dependsOn = ["mongos"]
	}
}