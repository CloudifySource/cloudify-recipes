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
import org.cloudifysource.dsl.context.ServiceContextFactory

def config = new ConfigSlurper().parse(new File("play-service.properties").toURL())
serviceContext = ServiceContextFactory.getServiceContext()
playRootFolder=serviceContext.serviceDirectory + "/${config.name}"
applicationDir="${playRootFolder}/playApps/${config.applicationName}"


def evolutionsStr=""
if ( config.applyEvolutions ) { 
	evolutionsStr="-DapplyEvolutions.default=true"
}

def startOrRun

if ( config.productionMode ) {
	startOrRun = "start"
}
else {
	startOrRun = "run"
}

/* Not sure if this is reqired and/or if this is the most suitable value */
def httpAddr="-Dhttp.address=0.0.0.0"

def argLine="${evolutionsStr} ${httpAddr} \"${startOrRun} ${config.httpPort}\""

new AntBuilder().sequential {
	echo(message:"Executing ${playRootFolder}/play ${argLine} in  ${applicationDir} ...")
	exec(executable:"${playRootFolder}/play", dir:"${applicationDir}", osfamily:"unix") {       
		arg(line:"${argLine}")			
	}	
}
       

