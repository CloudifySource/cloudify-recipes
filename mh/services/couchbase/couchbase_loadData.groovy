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
			This custom command enables users to load data into the Couchbase cluster
			Usage :  invoke couchbase loadData path_to_zipFile 
			Examples 
		      1. If file1.zip is located in http://www.myZips.com/file1.zip , use the following command :
					invoke couchbase loadData http://www.myZips.com/file1.zip 
			  2. If file1.zip is located in /tmp/file1.zip (on the remote machine), use the following command :			  
			        invoke couchbase loadData /tmp/file1.zip
		*/	

println "couchbase_loadData.groovy: Starting ..."

context = ServiceContextFactory.getServiceContext()

def instanceID = context.instanceId
if ( instanceID != 1 ) {
	return
}

def binFolder=context.attributes.thisInstance["homeFolder"]
def toolsFolder="${binFolder}/tools"
def cbDocLoader="${toolsFolder}/cbdocloader"

def clusterAdmin = context.attributes.thisInstance["couchbaseUser"]
def clusterPassword = context.attributes.thisInstance["couchbasePassword"]
def currentPort = context.attributes.thisInstance["currentPort"]
def clusterBucketName = "CloudifyCouchbase${instanceID}"

def origZipFile = args[0]
def currZipFile

builder = new AntBuilder()

if ( origZipFile =~/(?i)^(http|ftp|sftp).*/ ) {	
	currZipFile="${cbDocLoader}/currZipFile.zip"
	builder.sequential {	
		echo(message:"couchbase_loadData.groovy: Running ${addServerScript} ...")	
		echo(message:"Getting ${origZipFile} to ${currZipFile} ...")
		get(src:"${origZipFile}", dest:"${currZipFile}", skipexisting:false)
	}
}		
else {
	currZipFile=origZipFile
	println "couchbase_loadData.groovy: Using ${currZipFile}"
}

builder.sequential {	
	echo(message:"couchbase_loadData.groovy: Running ${cbDocLoader} ... ${currZipFile} ...")
	exec(executable:"${cbDocLoader}", outputproperty:"currentOutPut", failonerror: "true") {
		arg(line:"-u ${clusterAdmin} -p ${clusterPassword} -n 127.0.0.1:${currentPort} -b ${clusterBucketName} ${currZipFile}")	
	}
}	

def outputPropertyStr = builder.project.properties."currentOutPut"

println outputPropertyStr
return outputPropertyStr

println "couchbase_loadData.groovy: End of loadData script"