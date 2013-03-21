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
jbossConfig = new ConfigSlurper().parse(new File("jboss-service.properties").toURL())

println "jboss_install.groovy: installation folder is ${jbossConfig.installDir}"


new AntBuilder().sequential {
	mkdir(dir:jbossConfig.installDir)
	get(src:"${jbossConfig.downloadPath}", dest:"${jbossConfig.installDir}/${jbossConfig.zipName}", skipexisting:true)	
	unzip(src:"${jbossConfig.installDir}/${jbossConfig.zipName}", dest:"${jbossConfig.installDir}", overwrite:true)
	chmod(dir:"${jbossConfig.installDir}/${jbossConfig.name}/bin", perm:'+x', includes:"*.sh")

}

println "jboss_install.groovy: installation ended successfully"

