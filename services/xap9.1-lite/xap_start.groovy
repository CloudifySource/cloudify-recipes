/*******************************************************************************
* Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
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
import java.io.InputStream
import java.io.BufferedReader
import java.util.Arrays
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.context.ServiceContextFactory


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("xap-service.properties").toURL())
ip=InetAddress.getLocalHost().getHostAddress()

new AntBuilder().sequential {
	exec(executable:"runxap.bat", osfamily:"windows"){
		env(key:"GSC_JAVA_OPTIONS",value:"${config.gscSize}")
		env(key:"LOOKUPLOCATORS",value:"${ip}")
		env(key:"WEBUI_PORT",value:"${config.uiPort}")
	} 

	chmod(dir:"${context.serviceDirectory}",perm:"+x",includes:"*.sh")
	chmod(dir:"${config.binDir}",perm:"+x",includes:"*.sh")
	chmod(dir:"${config.xapDir}/tools/gs-webui",perm:"+x",includes:"*.sh")
	
	exec(executable:"./runxap.sh", osfamily:"unix"){
		env(key:"GSC_JAVA_OPTIONS",value:"-Xms${config.gscSize} -Xmx${config.gscSize}")
		env(key:"LOOKUPLOCATORS",value:"${ip}")
		env(key:"WEBUI_PORT",value:"${config.uiPort}")
		arg(value:"${config.binDir}")
	}
}


