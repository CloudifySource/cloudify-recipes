import java.util.concurrent.TimeUnit;

service {
	extend "../../../services/biginsights/data"

	name "secondaryNameNode"

	icon "biginsights.png"

    elastic false
	numInstances 1	

	compute {

		template "MASTER"

	}

	lifecycle {

		install "data_install.groovy"

		start "data_start.groovy" 	
		
		stop "data_stop.groovy"

		startDetectionTimeoutSecs 3000	

		startDetection {

			ServiceUtils.isPortOccupied(22)

		}	

		locator {			

			def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.eq=org.apache.hadoop.hdfs.server.namenode.NameNode")

			println ":secondaryNameNode-service.groovy: current PIDs: ${myPids}"

			return myPids

        }					

	}

		
}
