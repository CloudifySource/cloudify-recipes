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
import org.cloudifysource.dsl.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

def serviceContext = ServiceContextFactory.getServiceContext()
println "data_start.groovy: BigInsights data node is about to start"
println "${serviceContext.serviceDirectory}/wait_for_port.sh"

new AntBuilder().sequential {	
	chmod(file:"${serviceContext.serviceDirectory}/wait_for_port.sh", perm:"ugo+rx")
	exec(executable:"${serviceContext.serviceDirectory}/wait_for_port.sh", osfamily:"unix", failonerror:"true") {
	}
}
println "data_start.groovy: BigInsights data node is down"