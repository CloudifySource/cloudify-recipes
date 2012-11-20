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
import static Shell.*;
import java.util.concurrent.TimeUnit
import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import java.net.InetAddress;
import org.cloudifysource.dsl.context.ServiceContextFactory


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File("storm-service.properties").toURL())

def service = null

///
/// FIND ZOOKEEPER NODES
///

while (service == null)
{
   println "Locating zookeeper service...";
   service = context.waitForService("zookeeper", 120, TimeUnit.SECONDS)
}
def zooks = null;
def rowCount=0;
while(zooks==null)
{
   println "Locating zookeeper service instances. Expecting " + service.getNumberOfPlannedInstances();
   zooks = service.waitForInstances(service.getNumberOfPlannedInstances(), 120, TimeUnit.SECONDS )
}

println "Found ${zooks.length} zookeeper nodes"

///
/// FIND NIMBUS
///

service=null

while (service == null)
{
   println "Locating storm-nimbus service...";
   service = context.waitForService("storm-nimbus", 120, TimeUnit.SECONDS)
}
def nimbs = null;
rowCount=0;
while(nimbs==null)
{
   println "Locating nimbus service instance."
   nimbs = service.waitForInstances(1, 120, TimeUnit.SECONDS )
}

def nimbus = nimbs[0].hostAddress

///
/// EVAL TEMPLATE
///

def binding=["zooks":zooks,"nimbus":nimbus]
def yaml = new File('templates/storm.yaml')
engine = new SimpleTemplateEngine()
template = engine.createTemplate(yaml).make(binding)

///
/// SETUP STORM PREREQS ON NODE
///

sh "chmod +x initnode.sh"
sh "./initnode.sh"

new AntBuilder().sequential {
	mkdir(dir:"${config.installDir}")
	get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
	unzip(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true)
	//dos2unix on the linux script files
	fixcrlf(srcDir:"${config.installDir}/${config.name}/bin", eol:"lf", eof:"remove", excludes:"*.bat *.jar")
	delete(file:"${config.installDir}/${config.zipName}")

   //templates start scripts
	chmod(file:"${config.installDir}/${config.name}/bin/storm", perm:'ugo+rx')
	chmod(dir:"${config.installDir}/${config.name}/bin", perm:'ugo+rx', includes:"*.sh")
	delete(file:"${config.installDir}/${config.name}/conf/storm.yaml")
}

new File("${config.installDir}/${config.name}/conf/storm.yaml").withWriter{ out->
  out.write(template.toString())
}

