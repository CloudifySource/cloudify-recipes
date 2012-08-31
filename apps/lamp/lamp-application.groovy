application {
	name "lamp"

	service {
		name = "apacheLB"		
	}	
	
	service {
		name = "mysql"		
	}
		
	service {
		name = "apache"
		dependsOn = [ "mysql", "apacheLB" ]
	}	
}


