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
import org.cloudifysource.utilitydomain.context.ServiceContextFactory

jbossConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())
context = ServiceContextFactory.getServiceContext()

def currentIP = context.attributes.thisInstance["currentIP"]
def portIncrement =  context.isLocalCloud() ? context.getInstanceId()-1 : 0		
def currJmxPort = jbossConfig.jmxPort + portIncrement



script = "${jbossConfig.home}/bin/jboss-admin"
new AntBuilder().sequential {
	exec(executable:"${script}.sh", osfamily:"unix"){
		arg value:"---controller=${currentIP}:${currJmxPort}"
		arg value:"--connect"
		arg value:"--command=:shutdown"
	}
	exec(executable:"${script}.bat", osfamily:"windows"){
		arg value:"---controller=${currentIP}:${currJmxPort}"
		arg value:"--connect"
		arg value:"--command=:shutdown"
	}
}
