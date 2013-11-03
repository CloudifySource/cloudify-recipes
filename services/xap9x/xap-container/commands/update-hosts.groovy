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
import static Shell.*
import java.util.concurrent.TimeUnit
import org.cloudifysource.dsl.utils.ServiceUtils
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.openspaces.admin.AdminFactory
import org.openspaces.admin.application.config.ApplicationConfig
import org.openspaces.admin.pu.config.ProcessingUnitConfig
import groovy.util.ConfigSlurper;


/*
  Merges the supplied line with the /etc/hosts file
*/

if(ServiceUtils.isWindows())assert false,"NOT IMPLEMENTED FOR WINDOWS"


context=ServiceContextFactory.serviceContext
config = new ConfigSlurper().parse(new File(context.serviceName+"-service.properties").toURL())

def hostsline=context.attributes.thisInstance["update-hosts-hostsline"]
assert hostsline!=null, "no etc hosts entry supplied"

def address=hostsline[0]
def lines=[]
def found=false
new File("/etc/hosts").eachLine{ line->
	def toks=[]
	toks.addAll(line.split())
	if(toks.size()==0)return
	if(toks[0]==address){
		found=true
		//merge
		hostsline[1..-1].each{
			toks.add(it)
		}
	}
	lines.add(toks.join(" "))
}
new File("/tmp/hosts").withWriter{out->
	lines.each{ line->
		out.writeLine(line)
	}
	if(!found)out.writeLine(hostsline.join(" "))
}

sudo("mv /tmp/hosts /etc/hosts")

