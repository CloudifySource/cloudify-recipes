application {
	name="HttpSession"
	
	service {
		name = "apacheLB"		
	}
	
	service {
		name = "tomcat"
		dependsOn = ["apacheLB"]
	}

}
