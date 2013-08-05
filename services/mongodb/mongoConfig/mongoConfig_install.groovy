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
import org.cloudifysource.dsl.utils.ServiceUtils;

config = new ConfigSlurper().parse(new File("mongoConfig-service.properties").toURL())
osConfig = ServiceUtils.isWindows() ? config.win32 : config.unix

serviceContext = ServiceContextFactory.getServiceContext()
instanceID = serviceContext.getInstanceId()

installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + instanceID

println "mongoConfig_install.groovy: Writing mongoConfig port to this instance(${instanceID}) attributes..."

home = "${serviceContext.serviceDirectory}/mongodb-${config.version}"
script = "${home}/bin/mongod"

serviceContext.attributes.thisInstance["home"] = "${home}"
serviceContext.attributes.thisInstance["script"] = "${script}"
println "mongoConfig_install.groovy: mongoConfig(${instanceID}) home is ${home}"


serviceContext.attributes.thisInstance["port"]=config.port
port=serviceContext.attributes.thisInstance["port"]
println "mongoConfig_install.groovy: mongoConfig(${instanceID}) is using port ${port}"

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
builder.move(file:"${installDir}/${osConfig.name}", tofile:"${home}")

if(!ServiceUtils.isWindows()) {
	println "calling chmod on ${home}/bin"
	builder.chmod(dir:"${home}/bin", perm:'+x', includes:"*")
}

println "mongoConfig_install.groovy: Installation of mongoConfig(${instanceID}) ended"