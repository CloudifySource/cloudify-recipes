service {
	extend "../../services/couchbase"
	numInstances 1

	compute {
		template "SMALL_LINUX"
	}

	customCommands ([
		/* 
			This custom command enables users to add a server (to the 1st server).
			Usage :  invoke couchbase addServer newServerHost newServerPort newServerUser newServerPassword
			
			
			Example: invoke couchbase addServer 1234.543.556.33 8097 admin mypassword
		*/
	
		"addServer" : "couchbase_addServer.groovy" , 
		
		
		/* 
			This custom command enables users rebalance the Couchbase cluster
			Usage :  invoke couchbase rebalance 
		*/
	
		"rebalance" : "couchbase_rebalance.groovy" , 		
		
		/* 
			This custom command enables users to enable XDCR with another cluster instance
			Usage :  invoke couchbase xdcr localBucketName remoteClusterRefName remoteClusterNode1 remoteClusterPort remoteClusterUser remoteClusterPassword remoteBucketName replicationType
			
			Example: invoke couchbase xdcr appBucket apac-cluster 10.10.10.10 8091 admin mypassword appBucket continuous
		*/
	
		"xdcr" : "couchbase_xdcr.groovy" , 		
	])		
}
