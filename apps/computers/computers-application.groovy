application {
	name "computers"

	service {
		name = "mysql"		
	}
	
	service {
		name = "apacheLB"		
	}	
	
	service {
		name = "play"
		dependsOn = ["mysql","apacheLB"]
	}	
}


