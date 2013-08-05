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
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.utilitydomain.context.ServiceContextFactory


serviceContext = ServiceContextFactory.getServiceContext()

config = new ConfigSlurper().parse(new File("mongod-service.properties").toURL())
osConfig = ServiceUtils.isWindows() ? config.win32 : config.unix

instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID

home = "${serviceContext.serviceDirectory}/mongodb-${config.version}"


serviceContext.attributes.thisInstance["home"] = "${home}"
println "mongod_install.groovy: mongod(${instanceID}) home is ${home}"

serviceContext.attributes.thisInstance["script"] = "${home}/bin/mongod"
println "mongod_install.groovy: mongod(${instanceID}) script is ${home}/bin/mongod"

currPort=config.port
if (serviceContext.isLocalCloud()) {
	if (config.sharded) {
		currPort+=instanceID-1
	}
}

serviceContext.attributes.thisInstance["port"] = currPort

println "mongod_install.groovy: mongod(${instanceID}) port ${currPort}"


builder = new AntBuilder()
builder.sequential {
	mkdir(dir:"${installDir}")
	ServiceUtils.getDownloadUtil().get("${osConfig.downloadPath}", "${installDir}/${osConfig.zipName}", true, "${osConfig.hashDownloadPath}")
}

if(ServiceUtils.isWindows()) {
	builder.unzip(src:"${installDir}/${osConfig.zipName}", dest:"${installDir}", overwrite:true)
} else {
	builder.untar(src:"${installDir}/${osConfig.zipName}", dest:"${installDir}", compression:"gzip", overwrite:true)
}

println "mongod_install.groovy: mongod(${instanceID}) moving ${installDir}/${osConfig.name} to ${home}..."
builder.move(file:"${installDir}/${osConfig.name}", tofile:"${home}")

if(!ServiceUtils.isWindows()) {
	println "calling chmod on ${home}/bin"
	builder.chmod(dir:"${home}/bin", perm:'+x', includes:"*")
}

println "mongod_install.groovy: mongod(${instanceID}) ended"