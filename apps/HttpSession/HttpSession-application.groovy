application {
	name="HttpSession"
	
/*	service {
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
*/
	service {
		name = "apacheLB"		
	}

	
	service {
		name = "tomcat"
		dependsOn = ["apacheLB"]
		//dependsOn = [/*"pu",*/apacheLB"]
	}

}
