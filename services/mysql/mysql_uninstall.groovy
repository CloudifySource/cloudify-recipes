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
import org.cloudifysource.dsl.context.ServiceContextFactory
import org.hyperic.sigar.OperatingSystem


def uninstallLinuxMysql(context,builder,currVendor,uninstallScript) {
	builder.sequential {
		echo(message:"mysql_uninstall.groovy: Chmodding +x ${context.serviceDirectory} ...")
		chmod(dir:"${context.serviceDirectory}", perm:"+x", includes:"*.sh")

		echo(message:"mysql_uninstall.groovy: Running ${context.serviceDirectory}/${uninstallScript} os is ${currVendor}...")
		exec(executable: "${context.serviceDirectory}/${uninstallScript}",failonerror: "true")
	}
}

context = ServiceContextFactory.getServiceContext()

builder = new AntBuilder()

def os = OperatingSystem.getInstance()
def currVendor=os.getVendor()
switch (currVendor) {
		case ["Ubuntu", "Debian", "Mint"]:		
			context.attributes.thisInstance["binFolder"]="/usr/bin"
			uninstallLinuxMysql(context,builder,currVendor,"uninstallOnUbuntu.sh")
			break		
		case ["Red Hat", "CentOS", "Fedora", "Amazon",""]:	
			context.attributes.thisInstance["binFolder"]="/usr/bin"		
			uninstallLinuxMysql(context,builder,currVendor,"uninstall.sh")
			break					
		case ~/.*(?i)(Microsoft|Windows).*/:
			println "mysql_uninstall.groovy: Windows - Doing nothing"
			break
		default: throw new Exception("Support for ${currVendor} is not implemented")
}

println "mysql_uninstall.groovy: End"

