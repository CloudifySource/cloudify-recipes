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

println "couchbase_start.groovy: Starting ..."

context = ServiceContextFactory.getServiceContext()
def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
def currVendor=context.attributes.thisInstance["osType"]
def startScript
switch (currVendor) {
	case ["Ubuntu"]:			
		startScript="${scriptsFolder}/run.sh"
		break	
	case ["CentOS"]:			
		startScript="${scriptsFolder}/run.sh"
		break	
	case ["Windows"]:
		startScript="${scriptsFolder}/run.bat"
		break
	default: throw new Exception("Support for ${currVendor} is not implemented")
}

//
def clusterHost="localhost" 
def clusterPort = context.attributes.thisInstance["currentPort"] 
def clusterAdmin = context.attributes.thisInstance["couchbaseUser"] 
def clusterPassword = context.attributes.thisInstance["couchbasePassword"] 
def clusterRamSize = context.attributes.thisInstance["clusterRamSize"] 
def postStartRequired = context.attributes.thisInstance["postStartRequired"]
def clusterBucketName = context.attributes.thisInstance["clusterBucketName"] 
def clusterBucketType = context.attributes.thisInstance["clusterBucketType"] 
def clusterReplicatCount = context.attributes.thisInstance["clusterReplicatCount"] 
def instanceID = context.instanceId


builder = new AntBuilder()
builder.sequential {
	echo(message:"couchbase_start.groovy: Running ${startScript} ...")
	exec(executable:"${startScript}", failonerror: "true") {
		arg(value:"${clusterHost}")
		arg(value:"${clusterPort}")
		arg(value:"${clusterAdmin}")
		arg(value:"${clusterPassword}")
		arg(value:"${clusterRamSize}")
		arg(value:"${postStartRequired}")	
		arg(value:instanceID)
		arg(value:"${clusterBucketType}")
		arg(value:clusterReplicatCount)
	}	
}

if ( postStartRequired == "true" ) {
	/* In the next start (e.g.: after a failure), there's no need to run the post start actions again */
	context.attributes.thisInstance["postStartRequired"] = "false"
	if ( instanceID == 1 ) {
		context.attributes.thisService["firstInstanceID"] = instanceID
		context.attributes.thisInstance["readyForRebalance"]=true
	}
	else {	
		while ( context.attributes.thisService["firstInstanceID"] == null ) {
			println "couchbase_start.groovy: instance #${instanceID} waiting for firstInstanceID..."
			sleep 10000			
		}
								
		println "couchbase_start.groovy: Waiting for couchbase service..."
		def couchbaseService = context.waitForService("couchbase", 180, TimeUnit.SECONDS)
		println "couchbase_start.groovy: Waiting for couchbase service instances ..."
		def couchbaseInstances = couchbaseService.waitForInstances(couchbaseService.numberOfActualInstances, 180, TimeUnit.SECONDS)				
			
					
		def index
		def currInstance
		def firstInstance = null		
		def newServerHost
		def newServerPort
		def firstInstanceID = context.attributes.thisService["firstInstanceID"]
		def instancesCount = couchbaseInstances.length
		println "couchbase_start.groovy: ${instancesCount} instances are available..."		
		for (index=0; index < instancesCount; index++) { 
			println "couchbase_start.groovy: In loop b4 index ${index} ... "
			currInstance = couchbaseInstances[index]

			println "couchbase_start.groovy: In loop after index ${index} currInstance.getInstanceID = " + currInstance.getInstanceID() 
			if ( firstInstanceID == currInstance.getInstanceID()  ) {	
				try { 
					firstInstance = currInstance											
					newServerHost = context.attributes.thisInstance["myHostAddress"] 
					newServerPort = "" + clusterPort
					newServerAdmin =  clusterAdmin
					newServerPassword = clusterPassword
					println "couchbase_start.groovy: instanceID ${instanceID} is about to invoke the addServer custom command on instanceID ${firstInstanceID}: newServerHost=${newServerHost} newServerPort=${newServerPort}"
					def currResult = firstInstance.invoke("addServer", newServerHost as String, newServerPort as String, newServerAdmin as String,newServerPassword as String)
				}							
				catch (all) {		
					println "couchbase_start.groovy: Exception thrown from addServer: " + all
				} 	
				context.attributes.thisInstance["readyForRebalance"]=true				
			}			
		}


		if ( firstInstance != null ) {
			/* The last instance initiates rebalancing .*/ 
			def planned = couchbaseService.numberOfPlannedInstances
			def readyInstances = 0		
					
			println "couchbase_start.groovy: instanceID ${instanceID} , ${planned} planned instances"
			def couchbaseAttrInstances=context.attributes.couchbase.instances			
			couchbaseAttrInstances.each {
				if ( it.readyForRebalance == true ) {
					readyInstances++
				}				
			}
			
			println "couchbase_start.groovy: instanceID ${instanceID} , ${readyInstances} ready instances"
			if ( planned == readyInstances ) {
				println "couchbase_start.groovy: instanceID ${instanceID} about to rebalance ... "
				try {
					firstInstance.invoke("rebalance")
					println "couchbase_start.groovy: instanceID ${instanceID} after rebalance"
				}
				catch (allErrors) {		
					//println "couchbase_start.groovy: Exception thrown from rebalance: " + allErrors
				} 	
			}
			else {
				println "couchbase_start.groovy: instanceID ${instanceID} : Only ${readyInstances} (of ${planned}) are ready, so postponing the rebalance"
			}
		}
	}							
}



println "couchbase_start.groovy: End of Start script"