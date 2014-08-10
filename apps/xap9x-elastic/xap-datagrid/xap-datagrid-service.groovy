/*******************************************************************************
* Copyright (c) 2014 GigaSpaces Technologies Ltd. All rights reserved
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
import util
import javax.management.*
import javax.management.remote.*


service {

	name "xap-datagrid"
	type "APP_SERVER"
	icon "xap.png"
	elastic true
	numInstances 2
	minAllowedInstances 2
	maxAllowedInstances 20


	compute {
        	template "${template}"
	}

	def instanceId=context.instanceId
        def toolsLoaded=false
	def jmxConnections = [:]
	lifecycle{


		install "xap_install.groovy"

		start "xap_start.groovy"

	        postStart "xap_postStart.groovy"

	        postStop "xap_postStop.groovy"

		locator {
			uuid=context.attributes.thisInstance.uuid
			i=0
			while (uuid==null){
				Thread.sleep 1000
				uuid=context.attributes.thisInstance.uuid
				if (i>10){
					println "LOCATOR TIMED OUT"
					break
				}
				i=i+1
			}
			if(i>11)return null

			i=0
			def pids=[]
			while(pids.size()==0){
				pids=ServiceUtils.ProcessUtils.getPidsWithQuery("Args.*.ct=${uuid}");
				i++;
				if(i>10){
					println "PROCESS NOT DETECTED"
					break
				}
				Thread.sleep(1000)
			}
			return pids
		}
		monitors {
			if(!toolsLoaded){
				this.getClass().classLoader.addClasspath(System.getProperty( 'java.home') + '/../lib/tools.jar')
                              	this.getClass().classLoader.addClasspath(System.getProperty( 'java.home') + '/lib/tools.jar')
				toolsLoaded=true
                }

                CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress"
                jps = 'jps -vml'.execute()
                jps.waitFor()
                vmClass = Class.forName('com.sun.tools.attach.VirtualMachine', true, this.getClass().classLoader)
                vmAttach = vmClass.getMethod('attach', java.lang.String.class)
                newJMXConnections = [:]
 			memoryUsage =  jps.in.text
                        .split('\n')
                        	.grep({ it.contains('com.gigaspaces.start.services="GSC"')})
                            .grep({ it.contains('elasticZone')})
	                        .collect({ it.split(' ')[0]})
                        .collect({
                    vm = vmAttach.invoke(null, it);
                        	        try{
                        connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                        if (connectorAddress == null) {
                            agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
                            vm.loadAgent(agent);
                            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
                        }

                        return new JMXServiceURL(connectorAddress);
                    } finally {
                        vm.detach()
                    }
                })
                        .collect({
                    serverConnection = jmxConnections[it]
					if(serverConnection == null){
                        serverConnection = JMXConnectorFactory.connect(it).getMBeanServerConnection()
                    }
                    newJMXConnections[it] = serverConnection
        	                        heap = serverConnection.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage")
                            		heap.get("used")/heap.get("max")
	                        })
        	                .max()

			   jmxConnections = newJMXConnections
                return ["Memory usage":memoryUsage == null ? 0 : memoryUsage*100.0]
		}
	}


	customCommands ([
//Public entry points

		"update-hosts": {String...line ->
			util.invokeLocal(context,"_update-hosts", [
				"update-hosts-hostsline":line
			])
		 },


		//Actual parameterized calls
		"_update-hosts"	: "commands/update-hosts.groovy"

	])

    scaleCooldownInSeconds 600
    samplingPeriodInSeconds 1

	scalingRules ([
    		scalingRule {
			serviceStatistics {
				metric "Memory usage"
				movingTimeRangeInSeconds 20
        			statistics Statistics.maximumOfAverages
      			}
 
			highThreshold {
			        value 75
				instancesIncrease 1
      			}
 
			lowThreshold {
        			value 35
				instancesDecrease 1
      			}
    		}
	  ])

 	userInterface {
                metricGroups = ([
                        metricGroup {
                                name "Max Mem"
                                metrics([
                                        "Memory usage"
                                ])
                        }
                ])

                widgetGroups = ([
                        widgetGroup {
                                name "Memory usage"
                                widgets ([
                                        balanceGauge{metric = "Memory usage"},
                                        barLineChart{
                                                metric "Memory usage"
                                                axisYUnit Unit.REGULAR
                                        },
                                ])
                        }
		])

	}
/*	userInterface {
		metricGroups = ([
		]
		)

		widgetGroups = ([
		]
		)
	}
*/
    network {
        template "APPLICATION_NET"
        accessRules {
            incoming ([
                    accessRule {
                        type "APPLICATION"
                        portRange "4242-4342"
                    }
            ])
        }
    }
}


