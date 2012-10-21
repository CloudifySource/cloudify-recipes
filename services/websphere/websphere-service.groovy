service {
   
    name "websphere"
	icon "websphere_logo.png"
	type "APP_SERVER"
    elastic true
	numInstances 1
	minAllowedInstances 1
	maxAllowedInstances 2
	
	compute {
		template "SMALL_LINUX"
	}	

	lifecycle{	
	
		details {
			def currPublicIP
			
			if (  context.isLocalCloud()  ) {
				currPublicIP =InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP =System.getenv()["CLOUDIFY_AGENT_ENV_PUBLIC_IP"]
			}
			def wasURL	= "http://${currPublicIP}:${startingPort}"
			
			def plantsSampleURL = "${wasURL}/PlantsByWebSphere/"
			println "websphere-service.groovy: plantsSampleURL is ${plantsSampleURL}"
			
			def albumCatalogURL = "${wasURL}/AlbumCatalogWeb/AlbumCatalog.jsp"
			println "websphere-service.groovy: albumCatalogURL is ${albumCatalogURL}"

			def petClinicURL = "${wasURL}/petclinic-mongo/"
			println "websphere-service.groovy: petClinicURL is ${petClinicURL}"			
			
			def consolePort = startingPort+3
			def wasConsole = "https://${currPublicIP}:${consolePort}/ibm/console"
			println "websphere-service.groovy: wasConsole is ${wasConsole}"
										
            return [
				"WebSphere Console":"<a href=\"${wasConsole}\" target=\"_blank\">${wasConsole}</a>",
                "Planets Sample":"<a href=\"${plantsSampleURL}\" target=\"_blank\">${plantsSampleURL}</a>" , 
                "AlbumCatalog Sample":"<a href=\"${albumCatalogURL}\" target=\"_blank\">${albumCatalogURL}</a>" ,
                "Pet Clinic":"<a href=\"${petClinicURL}\" target=\"_blank\">${petClinicURL}</a>"
            ]
		}		
	
	
	
		install "websphere_install.groovy"		
		start "websphere_start.groovy"
		stop "websphere_stop.groovy"
		postStop "websphere_uninstall.groovy"
		
		startDetectionTimeoutSecs 7200
		startDetection {
			println "websphere-service.groovy: Testing port ${startingPort} ..."
			ServiceUtils.isPortOccupied(startingPort)            
		}	
	}
	
	userInterface {

		metricGroups = ([
			metricGroup {

				name "process"

				metrics([
				    "Total Process Cpu Time",
					"Process Cpu Usage",
					"Total Process Virtual Memory",
					"Num Of Active Threads"
				])
			}
		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Process Cpu Usage"
				widgets ([
					balanceGauge{metric = "Process Cpu Usage"},
					barLineChart{
						metric "Process Cpu Usage"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name "Total Process Virtual Memory"
				widgets([
					balanceGauge{metric = "Total Process Virtual Memory"},
					barLineChart {
						metric "Total Process Virtual Memory"
						axisYUnit Unit.MEMORY
					}
				])
			},
			widgetGroup {
				name "Num Of Active Threads"
				widgets ([
					balanceGauge{metric = "Num Of Active Threads"},
					barLineChart{
						metric "Num Of Active Threads"
						axisYUnit Unit.REGULAR
					}
				])
			}     ,
						
			widgetGroup {
				name "Total Process Cpu Time"
				widgets([
					balanceGauge{metric = "Total Process Cpu Time"},
					barLineChart {
						metric "Total Process Cpu Time"
						axisYUnit Unit.REGULAR
					}
				])
			}
		]
		)
	}
	
	network {
        port = startingPort
        protocolDescription ="HTTP"
    }	
}	
 

	