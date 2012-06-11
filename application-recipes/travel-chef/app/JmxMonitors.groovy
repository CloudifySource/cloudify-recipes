/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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

def static connectRMI(host, port) {
        return JmxFactory.connect(new JmxUrl("service:jmx:rmi:///jndi/rmi://${host}:${port}/jmxrmi")).MBeanServerConnection
}

def static getJMXAttribute(server, objectName, attributeName) {
	String[] names = server.queryNames(new ObjectName(objectName), null)
	if (names.length > 0)
			return (new GroovyMBean(server, names[0]))[attributeName]
	return null;
}