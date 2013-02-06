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

		/* 
			This custom command enables users to enabled xdcr with a remote cluster
			Usage :  invoke couchbase xdcr localBucketName remoteClusterRefName remoteClusterNode-Public-DNS-Name remoteClusterPort remoteClusterUser remoteClusterPassword remoteBucketName replicationType
			
			Example: invoke couchbase xdcr appBucket apac-cluster ec2-address-123-456-789-012.ap-southeast-1.compute.amazonaws.com 8091 admin mypassword appBucket continuous
		*/

println "couchbase_xdcr.groovy: Starting ..."

context = ServiceContextFactory.getServiceContext()

def instanceID = context.instanceId
if ( instanceID != 1 ) {
	return
}

def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]

def xdcrScript="${scriptsFolder}/xdcr.sh"
def firstInstancePort = context.attributes.thisInstance["currentPort"]

def clusterAdmin = context.attributes.thisInstance["couchbaseUser"]
def clusterPassword = context.attributes.thisInstance["couchbasePassword"]

def localBucketName = args[0]
println "couchbase_xdcr.groovy: localBucketName is " + localBucketName
def remoteClusterRefName = args[1]
println "couchbase_xdcr.groovy: remoteClusterRefName is " + remoteClusterRefName
remoteClusterNodeName =  args[2]
println "couchbase_xdcr.groovy: remoteClusterNodeName is " + remoteClusterNodeName
remoteClusterPort = args[3] 
println "couchbase_xdcr.groovy: remoteClusterPort is " + remoteClusterPort
remoteClusterUser = args[4] 
println "couchbase_xdcr.groovy: remoteClusterUser is " + remoteClusterUser
remoteClusterPassword = args[5] 
println "couchbase_xdcr.groovy: remoteClusterPassword is " + remoteClusterPassword
remoteBucketName = args[6] 
println "couchbase_xdcr.groovy: remoteBucketName is " + remoteBucketName
replicationType = args[7] 
println "couchbase_xdcr.groovy: replicationType is " + replicationType

builder = new AntBuilder()
builder.sequential {	
	echo(message:"couchbase_xdcr.groovy: Running ${xdcrScript} ...")
	exec(executable:"${xdcrScript}", failonerror: "true") {
		arg(value:firstInstancePort)	
		arg(value:"${clusterAdmin}")	
		arg(value:"${clusterPassword}")	
		arg(value:"${localBucketName}")	
		arg(value:"${remoteClusterRefName}")		
		arg(value:"${remoteClusterNodeName}")	
		arg(value:"${remoteClusterPort}")	
		arg(value:"${remoteClusterUser}")	
		arg(value:"${remoteClusterPassword}")	
		arg(value:"${remoteBucketName}")	
		arg(value:"${replicationType}")	
	}
}	

println "couchbase_xdcr.groovy: End of XDCR script"