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
import org.hyperic.sigar.OperatingSystem
import java.util.concurrent.TimeUnit
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory
import org.cloudifysource.domain.context.ServiceInstance;

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("couchbase-service.properties").toURL())

def instanceID = context.instanceId

if ( instanceID == 1 ) {
	context.attributes.thisService["firstInstanceID"] = null
	
	if ("dataPath" in config) {
		context.attributes.thisService["needToLoadData"] = true
		context.attributes.thisService["dataPath"] = config.dataPath
	}
	else {
		context.attributes.thisService["needToLoadData"] = false
	}
}

context.attributes.thisInstance["myHostAddress"]=context.getPrivateAddress()

context.attributes.thisInstance["readyForRebalance"]=false


def portIncrement =  context.isLocalCloud() ? instanceID-1 : 0			
def currentPort = config.couchbasePort + portIncrement

osConfig = ServiceUtils.isWindows() ? config.win32 : config.linux

println "couchbase_install.groovy: currentPort is ${currentPort}"
context.attributes.thisInstance["currentPort"] = currentPort
context.attributes.thisInstance["couchbaseStatsPort"] = config.couchbaseStatsPort
context.attributes.thisInstance["couchbaseUser"] = config.couchbaseUser
context.attributes.thisInstance["couchbasePassword"] = config.couchbasePassword
context.attributes.thisInstance["clusterRamSize"] = config.clusterRamSize

context.attributes.thisInstance["clusterBucketName"] = config.clusterBucketName
context.attributes.thisInstance["clusterBucketType"] = config.clusterBucketType
context.attributes.thisInstance["clusterReplicatCount"] = config.clusterReplicatCount
context.attributes.thisInstance["scriptsFolder"] = "${context.serviceDirectory}/scripts"
context.attributes.thisInstance["postStartRequired"] = "true"



def installLinuxCouchbase(context,builder,currVendor,installScript,scriptsFolder,install32,install64) {
	builder.sequential {
		echo(message: "couchbase_install.groovy: Chmodding +x ${scriptsFolder} ...")
		chmod(dir: "${scriptsFolder}", perm:"+x", includes:"*.sh")

		echo(message: "couchbase_install.groovy: Running ${scriptsFolder}/${installScript} os is ${currVendor}...")
		exec(executable: "${scriptsFolder}/${installScript}",failonerror: "true") {
			arg(value:"${install32}")			
			arg(value:"${install64}")
		}
	}
}

def installWindowsCouchbase(config,osConfig,downloadFile,builder,scriptsFolder,install32,install64) {
	/* 
	downloadFolder=System.properties["user.home"]+ "/.cloudify"
	zipsDir = "${downloadFolder}/couchbase"
	downloadFile = "${zipsDir}/couchbase2.zip"
	installFolder="${context.serviceDirectory}/install"
	couchbaseRootFolder="${installFolder}/couchbase2"
	
	builder.sequential {
			echo(message:"couchbase_install.groovy: Creating zipsDir ${zipsDir} ...")
			mkdir(dir:"${zipsDir}")
			echo(message:"couchbase_install.groovy: installing on Windows...")
			echo(message:"couchbase_install.groovy: Creating installFolder ${installFolder} ...")
			mkdir(dir:"${installFolder}")
			mkdir(dir:"${downloadFolder}")
			echo(message:"couchbase_install.groovy: Getting ${osConfig.downloadUrl} to ${downloadFile} ...")
			get(src:"${osConfig.downloadUrl}", dest:"${downloadFile}", skipexisting:true)
			unzip(src:"${downloadFile}", dest:"${installFolder}", overwrite:true)
			copy( todir:"${installFolder}" ) {
				fileset( dir:'overrides-win' )
			}	
	}  */
	
}


builder = new AntBuilder()
def scriptsFolder = context.attributes.thisInstance["scriptsFolder"]
def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
def install32
def install64
switch (currVendor) {
	case ["Ubuntu", "Debian", "Mint"]:	
		install32=config.couchbaseDebUbuntu32
		install64=config.couchbaseDebUbuntu64
		installLinuxCouchbase(context,builder,currVendor,"installOnUbuntu.sh",scriptsFolder,install32,install64)
		context.attributes.thisInstance["homeFolder"]="/opt/couchbase/bin"
		context.attributes.thisInstance["osType"] = "Ubuntu"
		break		
	case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:
		install32=config.couchbaseRpmLinux32
		install64=config.couchbaseRpmLinux64
		installLinuxCouchbase(context,builder,currVendor,"install.sh",scriptsFolder,install32,install64)			
		context.attributes.thisInstance["homeFolder"]="/opt/couchbase/bin"	
		context.attributes.thisInstance["osType"] = "CentOS"		
		break					
	case ~/.*(?i)(Microsoft|Windows).*/:		
		install32=config.couchbaseExeWindows32
		install64=config.couchbaseExeWindows64
		installWindowsCouchbase(config,osConfig,downloadFile,builder,scriptsFolder,install32,install64)	
		context.attributes.thisInstance["osType"] = "Windows"		
		break
	default:
		install32=config.couchbaseRpmLinux32
		install64=config.couchbaseRpmLinux64
		installLinuxCouchbase(context,builder,currVendor,"install.sh",scriptsFolder,install32,install64)			
		context.attributes.thisInstance["homeFolder"]="/opt/couchbase/bin"	
		context.attributes.thisInstance["osType"] = "CentOS"
		break
}

println "couchbase_install.groovy: End of installation"