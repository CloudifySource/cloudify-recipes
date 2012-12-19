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
			This custom command enables users rebalance the Couchbase cluster
			Usage :  invoke couchbase rebalance
		*/

println "couchbase_rebalance.groovy: Starting ..."

context = ServiceContextFactory.getServiceContext()


def instanceID = context.instanceId

if ( instanceID != 1 ) {
	return
}

def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]

def rebalanceScript="${scriptsFolder}/rebalance.sh"
def firstInstancePort = context.attributes.thisInstance["currentPort"]

def clusterAdmin = context.attributes.thisInstance["couchbaseUser"]
def clusterPassword = context.attributes.thisInstance["couchbasePassword"]


builder = new AntBuilder()
builder.sequential {	
	echo(message:"couchbase_rebalance.groovy: Running ${rebalanceScript} ...")
	exec(executable:"${rebalanceScript}", failonerror: "true") {
		arg(value:"localhost")
		arg(value:firstInstancePort)	
		arg(value:"${clusterAdmin}")	
		arg(value:"${clusterPassword}")
	}
}	


println "couchbase_rebalance.groovy: End of rebalance script"