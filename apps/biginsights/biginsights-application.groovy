application {
	name="biginsights"
	
	service {
		name = "master"		
		dependsOn = ["data"]//,"hbase","flume"]
	}
		
	service {
		name = "data"
	}
/*	service {
		name = "hbase"
	}
	service {
		name = "flume"
	}
	
	service {
		name = "mongod"		
	}
	
	service {
		name = "mongoConfig"		
	}
	
	service {
		name = "mongos"
		dependsOn = ["mongoConfig", "mongod"]
	}*/
}
