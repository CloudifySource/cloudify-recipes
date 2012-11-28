/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
import javax.management.ObjectName
import javax.management.remote.JMXConnectorFactory as JmxFactory
import javax.management.remote.JMXServiceURL as JmxUrl

/**
 * Connect to a JMX server over RMI
 */
 
class JmxMonitors {

	def static urlToConnection = [:]
		
	def static connect(url) {	
		println "JmxMonitors.connect: Using ${url}"
		if (urlToConnection[url] == null)
			urlToConnection[url] = JmxFactory.connect(new JmxUrl(url)).MBeanServerConnection
		return urlToConnection[url]
	} 

	
	/**
	 * Get a JMX attribute
	 */
	def static getJMXAttribute(connection, objectName, attributeName) {		
		String[] names = connection.queryNames(new ObjectName(objectName), null)
		if (names.length > 0)
				return (new GroovyMBean(connection, names[0]))[attributeName]
		return 0;
	}
			
		/* Returns a map of metrics values */ 
	def static getJmxMetrics(host,port,metricNamesToMBeansNames) {
		def url = "service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi"
		getJmxMetrics(url, metricNamesToMBeansNames)
	}
	
	def static getJmxMetrics(url,metricNamesToMBeansNames) {
		def connection = connect(url) 
		def metrics  = [:]
	
		metricNamesToMBeansNames.each{metricName,objectsArr->
			def objectName=objectsArr[0]
			def attributeName=objectsArr[1]
			def currMetricValue = getJMXAttribute(connection,objectName , attributeName) 		
			metrics.put(metricName,currMetricValue)
		}
	
		return metrics
	}	
	
}

