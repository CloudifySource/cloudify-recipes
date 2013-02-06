application {
	name="medici"

	service {
		name = "couchbase"		
	}

	service {
		name = "elasticsearch"		
	}

	service {
		name = "tomcat"
		dependsOn = ["couchbase", "elasticsearch"]
	}

}