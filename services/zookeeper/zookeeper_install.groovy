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
import java.util.concurrent.TimeUnit
import groovy.text.SimpleTemplateEngine
import groovy.util.ConfigSlurper;
import org.cloudifysource.dsl.context.ServiceContextFactory


def config = new ConfigSlurper().parse(new File("zookeeper-service.properties").toURL())
def context=ServiceContextFactory.serviceContext
def service = null

while (service == null)
{
   println "Locating zookeeper service...";
   service = context.waitForService("zookeeper", 120, TimeUnit.SECONDS)
}
def zkInstances = null;
def rowCount=0;
while(zkInstances==null)
{
   zkInstances = service.waitForInstances(service.getNumberOfPlannedInstances(), 120, TimeUnit.SECONDS )
}
def ips=[]

zkInstances.eachWithIndex{ instance,i ->
	if (instance.hostAddress == "127.0.0.1"){
		ips.add(InetAddress.localHost.hostAddress)
	}
	else{
	  	ips.add(instance.hostAddress)
	}
}

// The following sort is needed so every node has same id->host mapping
ips.sort()
def myid=0
for(i in 1..ips.size()){
println "INET ADDR=${InetAddress.localHost.hostAddress}";
	if(ips.get(i-1)==InetAddress.localHost.hostAddress){
		myid=i;break;
	}
}

def binding=["hosts":ips,"clientPort":"${config.clientPort}"]
def zoo = new File('templates/zoo.cfg')
engine = new SimpleTemplateEngine()
template = engine.createTemplate(zoo).make(binding)

new AntBuilder().sequential {
	mkdir(dir:"${config.installDir}")
	mkdir(dir:"/tmp/zookeeper")
	get(src:config.downloadPath, dest:"${config.installDir}/${config.zipName}", skipexisting:true)
	untar(src:"${config.installDir}/${config.zipName}", dest:config.installDir, overwrite:true, compression:"gzip")
	//dos2unix on the linux script files
	fixcrlf(srcDir:"${config.installDir}/${config.name}/bin", eol:"lf", eof:"remove", excludes:"*.bat *.jar")
	delete(file:"${config.installDir}/${config.zipName}")

   //use default config
	move(file:"${config.installDir}/${config.name}/conf/zoo_sample.cfg", tofile:"${config.installDir}/${config.name}/conf/zoo.cfg");

   //templates start scripts
	move(file:"templates/zkServer.cmd", todir:"${config.installDir}/${config.name}/bin")
	move(file:"templates/zkServer.sh", todir:"${config.installDir}/${config.name}/bin")
	chmod(dir:"${config.installDir}/${config.name}/bin", perm:'ugo+rx', includes:"*.sh")
	chmod(dir:"${context.serviceDirectory}", perm:"ugo+rx", includes:"*.sh", verbose: true)
	delete(file:"${config.installDir}/${config.name}/conf/zoo.cfg")
}
new File("${config.installDir}/${config.name}/conf/zoo.cfg").withWriter{ out->
out.write(template.toString())
}

//create myid file
new File("/tmp/zookeeper/myid").createNewFile()
new File("/tmp/zookeeper/myid").withWriter{ out->
out.write(myid.toString())
}
