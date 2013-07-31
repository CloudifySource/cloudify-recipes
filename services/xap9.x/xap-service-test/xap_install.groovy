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
import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.dsl.context.ServiceContextFactory


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("xap-service.properties").toURL())


fw=new FileWriter("c:\\Users\\DeWayne\\Documents\\install.out")
fw.append("start")
fw.append("instance id=${context.instanceId}")
fw.close()

new AntBuilder().sequential {
	mkdir(dir:"${config.installDir}")
	get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
}


// Update gs ui port
if(ServiceUtils.isWindows()){
  new AntBuilder().sequential {
	copy(file:"overwrite/gs-webui.bat",todir:"${config.installDir}/${config.xapDir}/tools/gs-webui",overwrite:"true")
  }
}
else{
  new AntBuilder().sequential {
	copy(file:"overwrite/gs-webui.sh",todir:"${config.xapDir}/tools/gs-webui",overwrite:"true")
   chmod(dir:"${config.installDir}/${config.xapDir}/bin", perm:"+x", includes:"*.sh")
   chmod(dir:"${config.installDir}/${config.xapDir}/tools/gs-webui", perm:"+x", includes:"*.sh")
  }
}

