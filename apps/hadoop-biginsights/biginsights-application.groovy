application {
	name="biginsights"
	
	service {
		name = "master"		
		dependsOn = ["data","hbase"]
	}		
	service {
		name = "data"
	}
	service {
		name = "hbase"
	}
}
