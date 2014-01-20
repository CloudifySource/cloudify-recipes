import java.util.concurrent.TimeUnit;

service {
	extend "../../../services/biginsights/data"

	name "dataOnDemand"

	icon "biginsights.png"

    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 5	

	compute {

		template "SMALL_LINUX"

	}

	lifecycle {

		install "data_install.groovy"

		start "data_start.groovy" 	
		def instanceID = context.instanceId	
		postStart {
			println "dataOnDemand-service.groovy: dataOnDemand Post-start ..."
			def masterFound=false;
			def masterService = null;
			while(!masterFound){
				try{
					 masterService = context.waitForService("master", 180000, TimeUnit.SECONDS)			
					} catch(Exception e){
						println("Master is still launching");
					}
					if(masterService != null)
					{
						masterFound=true;
						println("Master was found successfully");
					}
			}
			def fulladdress= context.getPrivateAddress()
			def privateIP = fulladdress.split("/")[0]	
			println "dataOnDemand-service.groovy: privateIP is ${privateIP} ..."
			def objParamsHadoop = new Object[3] 
			objParamsHadoop[0] = privateIP as String
			objParamsHadoop[1] = "hadoop"
			objParamsHadoop[2] = instanceID as String
			
			def objParamsHbase = new Object[3] 
			objParamsHbase[0] = privateIP as String
			objParamsHbase[1] = "hbase"
			objParamsHbase[2] = instanceID as String
			masterService.invoke("addNode",objParamsHadoop ,2000, TimeUnit.SECONDS)
			masterService.invoke("addNode",objParamsHbase,2000, TimeUnit.SECONDS)
			
			def pathToInstallFile = context.serviceDirectory + "/installationRunning"
			def installRunFile = new File(pathToInstallFile)
			if ( installRunFile.exists()) {
				println "dataOnDemand-service.groovy: About to delete ${pathToInstallFile} ..."
				def successInDel = installRunFile.delete()
				println "dataOnDemand-service.groovy: Deleted ${pathToInstallFile} - success = ${successInDel}"
			}
			else {
				println "dataOnDemand-service.groovy: ${pathToInstallFile} does not exist. Skipping its deletion"
			}
										
			println "dataOnDemand-service.groovy: dataOnDemand Post-start ended"						
		}		

		preStop {
			println "dataOnDemand-service.groovy: dataOnDemand Pre-stop ..."
			def masterService = context.waitForService("master", 180, TimeUnit.SECONDS)			
			def privateIP
			privateIP =System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"]
			println "dataOnDemand-service.groovy: privateIP is ${privateIP} ..."
			masterService.invoke("removeNode", privateIP as String, instanceID as String)
			println "dataOnDemand-service.groovy: dataOnDemand Pre-stop ended"
			new AntBuilder().sequential {	
				chmod(file:"${context.serviceDirectory}/data_stop.sh", perm:"ugo+rx")
				exec(executable:"${context.serviceDirectory}/data_stop.sh", osfamily:"unix", failonerror:"false") {
				}
			}
		}

		startDetectionTimeoutSecs 3000	

		startDetection {

			ServiceUtils.isPortOccupied(22)

		}	

		locator {			

			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.eq=org.apache.hadoop.hdfs.server.datanode.DataNode")

			println ":data-service.groovy: current PIDs: ${myPids}"

			return myPids

        }					

	}

		
}
