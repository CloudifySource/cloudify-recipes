application {
	name="biginsights"
	
	service {
		name = "master"		
		dependsOn = ["data"/*,"secondaryNameNode"*/]
	}		
	service {
		name = "data"
	}
	/*service {
		name = "secondaryNameNode"
	}*/
	service {
		name = "dataOnDemand"
		dependsOn = ["master"]
	}
}
