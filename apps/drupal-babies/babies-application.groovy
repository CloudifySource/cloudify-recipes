application {
	name "babies"

	service {
		name = "mysql"		
	}
		
	service {
		name = "drupal"
		dependsOn = [ "mysql" ]
	}	
}


