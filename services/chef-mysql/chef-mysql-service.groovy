import java.util.concurrent.TimeUnit
import groovy.json.JsonOutput

service { 
    extend "../chef"
    name "chef-mysql"
    numInstances 1

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle {
        startDetectionTimeoutSecs 900
        startDetection {
            ServiceUtils.isPortOccupied(3306)
        }
		locator {
           NO_PROCESS_LOCATORS
        }
        start {
            // Couldn't find a way to read the port and scheme from PU properties
            def management_rest_url = 
                "http://" + \
                context.admin.processingUnits.waitFor("rest", 1000, TimeUnit.SECONDS).instances[0].machine.hostAddress + \
                ":8100/"

            Shell.sudo("mkdir -p /opt/cloudify")
            Shell.sudoWriteFile("/opt/cloudify/metadata.json", JsonOutput.toJson([
                    "management_rest_url": management_rest_url,
                    "application_name": context.applicationName, 
                    "service_name": context.serviceName,
                    "instance_id": context.instanceId
                ]))
            ChefBootstrap.getBootstrap(
                    context: context
            ).runSolo([
                "mysql": [ 
                    "server_root_password": "somepass",
                    "server_repl_password": "somepass",
                    "server_debian_password": "somepass"
                ],
                "run_list": ["recipe[cloudify_mysql::server]"]
            ])
        }
    }


}
