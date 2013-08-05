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
@Grab(group='com.gmongo', module='gmongo', version='0.8')
import com.gmongo.GMongo

import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import java.util.concurrent.TimeUnit

config = new ConfigSlurper().parse(new File('mongos-service.properties').toURL())

println "mongos_poststart.groovy: sleeping for 60 secs..."
sleep(60)

serviceContext = ServiceContextFactory.getServiceContext()

println "mongos_poststart.groovy: serviceContext object is " + serviceContext

//waiting for mongod service to become available, will not be needed in one of the upcoming builds 
println "mongos_poststart.groovy: Waiting for mongod..."
mongodService = serviceContext.waitForService("mongod", 20, TimeUnit.SECONDS)
if (mongodService == null) {
	throw new IllegalStateException("mongod service not found. mongod must be installed before mongos.");
}
mongodHostInstances = mongodService.waitForInstances(mongodService.numberOfPlannedInstances, 60, TimeUnit.SECONDS) 

if (mongodHostInstances == null) {
	throw new IllegalStateException("mongod service instances are not ready. They must be ready before mongos. Increasing the timeout in waitForInstances above, might solve it");
}


println "mongos_poststart.groovy: mongodHostInstances length is "+mongodHostInstances.length

currPort = serviceContext.attributes.thisInstance["port"] as int
println "mongos_poststart.groovy: Connecting to mongos on port ${currPort} ..."

mongo = new GMongo("127.0.0.1", currPort)
	
println "mongos_poststart.groovy: After new GMongo port ${currPort} ..."
	
db = mongo.getDB("admin")
assert db != null 	
println "Connection succeeded"	
	
	
mongodInstances=serviceContext.attributes.mongod.instances		
	
mongodHostInstances.each {
		mongodPort = mongodInstances[it.instanceId].port		
		mongodHost = it.hostAddress		
		println "mongos_poststart.groovy: mongod #"+it.instanceId + " host and port = ${mongodHost}:${mongodPort}"		
		result = db.command(["addshard":"${mongodHost}:${mongodPort}"])		
		println "mongos_poststart.groovy: db result: ${result}"
}
    
	
result = db.command(["enablesharding":"petclinic"])	
println "mongos_poststart.groovy: db result: ${result}"
	
result = db.command(["shardcollection":"petclinic.Person", "key":["_id":1]])
println "mongos_poststart.groovy: db result: ${result}"
	

 


