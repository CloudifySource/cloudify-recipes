
application {
	name="big_data_app"
	
	service {
		name = "feeder"
		dependsOn = ["processor"]
	}
	service {
		name = "processor"
		dependsOn = ["cassandra"]		
	}
	service {
		name = "cassandra"	
	}
	
	
}