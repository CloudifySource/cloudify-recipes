import javax.management.*
import javax.management.remote.*

CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress"
 jps = 'jps -vlm'.execute()
 jps.waitFor()
 memoryUsage =  jps.in.text
                        .split('\n')
                        .grep({ it.contains('com.gigaspaces.start.services="GSC"')})
                        .grep({ it.contains('elasticZone')})
                        .collect({ it.split(' ')[0]})
                        .collect({
                                vm = com.sun.tools.attach.VirtualMachine.attach(it)
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
				heap = JMXConnectorFactory.connect(it)
							.getMBeanServerConnection()
							.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage")
				heap.get("used")/heap.get("max")
			}) 
			.max()
   return memoryUsage
