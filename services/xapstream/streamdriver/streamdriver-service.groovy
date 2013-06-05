/******************************************************************************
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
import java.util.concurrent.TimeUnit
import util

service {

	name "streamdriver"
	type "APP_SERVER"
	//icon "icon.jpg"
	numInstances 1 
	maxAllowedInstances 99       //currently only 1 instance supported
	minAllowedInstances 1

	lifecycle{
		locator {
			NO_PROCESS_LOCATORS
		}

	}

	customCommands ([

		"help":{
			"""

list
  list streams in \"xapstream\" app in \"streamspace\" space

list-explicit
  list stream in supplied locator and spacename

create-stream stream-name field-names...
  create stream \"stream-name\" in default stream space

write-random count stream-name
  write random tuples to the given stream at the given rate
  uses xapstream service for target and streamspace for space and
  the defined tuple for streamspace. writes individual tuples as
  fast as it can
"""
		},

		//
		// LIST COMMANDS
		//
		/* List that assumes defaults */

		"list": {
			xapinstance=util.getServiceInstances(context,"xapstream",1)[0]
			util.invokeLocal(context,"_list",[locator:xapinstance.getHostAddress(),
									spacename:"streamspace"])
			return true
		},

		/* List that is specific about locator and spacename */

		"list-explicit": { locator,spacename->
			util.invokeLocal(context,"_list",[locator:locator,spacename:spacename])
			return true
		} ,
		 
		"_list": "commands/list-streams.groovy",

		//
		// CREATE COMMANDS
		//

		"create-stream": {
			println "NOT IMPLEMENTED"
		},

		//
		// WRITE COMMANDS
		//

		"write-random": { count,streamname ->
			xapinstance=util.getServiceInstances(context,"xapstream",1)[0]
			util.invokeLocal(context,"_write-random", [ locator:xapinstance.getHostAddress(),space:"streamspace",count:count,streamname:streamname])
			return true
		},

		"write-sentences": { count, streamname ->
			xapinstance=util.getServiceInstances(context,"xapstream",1)[0]
			util.invokeLocal(context,"_write-sentences", [ locator:xapinstance.getHostAddress(),space:"streamspace",count:count,streamname:streamname])
			return true
		},

		"_write-random": "commands/write-random.groovy",
		"_write-sentences": "commands/write-sentences.groovy",

	])
}


