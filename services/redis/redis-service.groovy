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
@Grab(group='redis.clients', module='jedis', version='2.0.0')
import redis.clients.jedis.Jedis

service {

	name "redis"
	type "MESSAGE_BUS"
	icon "redis.png"

	lifecycle{
		init "redis_install.groovy"
	    start "redis_start.groovy"
	    
	    monitors {
			map = [:]
			new Jedis("localhost").info().splitEachLine(":") { k,v -> map[(k)] = v } 
			
			return ["keyspace hits": map["keyspace_hits"], "keyspace misses": map["keyspace_misses"]]
			
	     }
	}
	plugins([
		plugin {
			name "portLiveness"
			className "org.cloudifysource.usm.liveness.PortLivenessDetector"
			config ([
						"Port" : [6379],
						"TimeoutInSeconds" : 60,
						"Host" : "127.0.0.1"
					])
		},
	])


	userInterface {
		metricGroups = ([
			metricGroup {

				name "redis"

				metrics([
					"keyspace hits", "keyspace misses",
				])
			},
		]
		)

	/*	widgetGroups = ([
			widgetGroup {
				name "keyspace hits"
				widgets ([
					barLineChart{
						metric "keyspace hits"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
		]
		)*/
	}
}


