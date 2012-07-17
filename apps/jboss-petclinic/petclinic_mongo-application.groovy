application {
	name="petclinic-mongo"
	
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
		name = "apacheLB"		
	}
	
	service {
		name = "jboss"
		dependsOn = ["mongos","apacheLB"]
	}
}