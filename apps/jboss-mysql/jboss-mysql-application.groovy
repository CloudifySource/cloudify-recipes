application {
	name="jboss-mysql"
	
	service {
		name = "mysql"		
	}
	service {
		name = "jboss"
		dependsOn = ["mysql"]
	}
}