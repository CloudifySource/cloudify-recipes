service {
	
	name "memcached"
	icon "memcached.png"
	type "NOSQL_DB"
	
	elastic true
	numInstances 2
	minAllowedInstances 2
	maxAllowedInstances 8
	
	def portIncrement =  context.isLocalCloud() ? context.getInstanceId()-1 : 0
	def currPort = port + portIncrement
	
	compute {
		template "SMALL_LINUX"
	}
	
	lifecycle{
		details {
			
			def currPublicIP
			
			if (context.isLocalCloud()) {
				currPublicIP = InetAddress.localHost.hostAddress
			}
			else {
				currPublicIP = context.getPublicAddress()
			}
			
			println "memcached-service.groovy:details: IP Address is ${currPublicIP}; currPort is ${currPort}."
			
			return [
				"IP Address": "${currPublicIP}",
				"Port": "${currPort}"
				]
			
		}
		
		preServiceStart "memcached_preServiceStart.groovy" // If the first instance is stopped manually from cloudstack, cloudify will try to bring it up, cloudify executes the preService start step.
		
		install "memcached_install.groovy"
		
		start "memcached_start.groovy"
		
		postStart "memcached_postStart.groovy"
		
		startDetectionTimeoutSecs 9000
		startDetection {
			return ServiceUtils.isPortOccupied(currPort)
		}
		
		stopDetection {
			return !ServiceUtils.isPortOccupied(currPort)
		}
		
		locator {	
			def memcachedPids =  ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=memcached,Args.*.eq=${currPort}")
			println "memcached-service.groovy: Current PIDs: ${memcachedPids}"
			return memcachedPids
			
		}
		
		preStop "memcached_preStop.groovy"
	}
	
	userInterface {
		
		metricGroups = ([
			metricGroup {
		
				name "process"
		
				metrics([
					"Total Process Cpu Time",
					"Process Cpu Kernel Time",
					"Total Process Residential Memory",
					"Total Num of Page Faults"
				])
			}
		]
		)
		
		widgetGroups = ([
			widgetGroup {
				name "Total Process Cpu Time"
				widgets([
					balanceGauge{metric = "Total Process Cpu Time"},
					barLineChart {
						metric "Total Process Cpu Time"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name "Process Cpu Kernel Time"
				widgets([
					balanceGauge{metric = "Process Cpu Kernel Time"},
					barLineChart {
						metric "Process Cpu Kernel Time"
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
				name "Total Process Virtual Memory"
				widgets([
					balanceGauge{metric = "Total Process Virtual Memory"},
					barLineChart {
						metric "Total Process Virtual Memory"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name = "memory"
				widgets = [
					balanceGauge { metric = "memory" },
					barLineChart{ metric = "memory"
						axisYUnit Unit.PERCENTAGE
					}
				]
			},
			widgetGroup {
				name "Total Process Residential Memory"
				widgets([
					balanceGauge{metric = "Total Process Residential Memory"},
					barLineChart {
						metric "Total Process Residential Memory"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name "Total Num of Page Faults"
				widgets([
					balanceGauge{metric = "Total Num of Page Faults"},
					barLineChart {
						metric "Total Num of Page Faults"
						axisYUnit Unit.PERCENTAGE
					}
				])
			}
		]
		)
	}
	
	
	scaleCooldownInSeconds 60
	samplingPeriodInSeconds 1
	
	scalingRules ([
		scalingRule {

			serviceStatistics {
				metric "Total Process Cpu Time"
				timeStatistics Statistics.averageCpuPercentage
				instancesStatistics Statistics.maximum
				movingTimeRangeInSeconds 20
			}

			highThreshold {
				value 40
				instancesIncrease 1
			}

			lowThreshold {
				value 20
				instancesDecrease 1
			}
		}
	])
	
}