/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.ProcCpu;

service {
        extend "../cognos"
        name "application-tier" 
        type "APP_SERVER"
        elastic true
		numInstances 1
		minAllowedInstances 1
		maxAllowedInstances 4		
        
        def currPort = 9300
        
        lifecycle{      

				monitors {
					def processCpuUsage = 0 as double								
					try { 			
					
						/* http://support.hyperic.com/display/SIGAR/PTQL */
						def currentPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.re=catalina|DQServer|CGSServer")
						
						if ( null == currentPids ) {
							processCpuUsage = 0 
							println "current Pids is null  : processCpuUsage is 0 ..."
						}
						else {
							if ( currentPids.size == 0 ) {
								processCpuUsage = 0 
								println "There are no Pids : processCpuUsage is 0 ..."								
							}
							else {
								def currProcessCPU = 0 as double
								currentPids.each{
									def sigar = ServiceUtils.ProcessUtils.getSigar()
									def cpu = sigar.getProcCpu(it) as ProcCpu
									currProcessCPU = cpu.getPercent() as double
									processCpuUsage += 100*currProcessCPU
									//println "processCpuUsage is ${processCpuUsage} ..."
								}
							}
						}
																										
						return [ "Cognos Cpu Usage": processCpuUsage ]
					}			
					finally { }
				}
		
                preStart "application-tier_preStart.groovy" 
                                
                startDetection {
                        println "application-tier-service.groovy(startDetection): checking port ${currPort} ..."
                        ServiceUtils.isPortOccupied(currPort)
                }

                stopDetection { 
                        !ServiceUtils.isPortOccupied(currPort)
                } 

				locator {			
					def myPids = ServiceUtils.ProcessUtils.getPidsWithQuery("State.Name.eq=java,Args.*.re=catalina|DQServer|CGSServer")
					return myPids
				}				
                
        } 
		
		userInterface {
		
			metricGroups = ([
				metricGroup {
					name "Cognos Cpu Usage"
					metrics([
						"Cognos Cpu Usage"
					])
				} 
			])

			widgetGroups = ([			
				widgetGroup {
					name "Cognos Cpu Usage"
					widgets ([
						balanceGauge{metric = "Cognos Cpu Usage"},
						barLineChart{
							metric "Cognos Cpu Usage"
							axisYUnit Unit.PERCENTAGE
						}
					])
				}
				
			])
		}
		
		
		scaleCooldownInSeconds 600
		samplingPeriodInSeconds 10
	
		scalingRules ([
			scalingRule {

				serviceStatistics {
					metric "Cognos Cpu Usage"
					timeStatistics Statistics.average
					instancesStatistics Statistics.maximum
					movingTimeRangeInSeconds 30
				}

				highThreshold {
					value 10
					instancesIncrease 1
				}

				lowThreshold {
					value 1
					instancesDecrease 1
				}
			}	
		])	
}

