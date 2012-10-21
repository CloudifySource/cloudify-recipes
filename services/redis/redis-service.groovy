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


