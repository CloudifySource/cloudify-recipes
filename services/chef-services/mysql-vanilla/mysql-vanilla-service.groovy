//import java.net.InetAddress 

service {
    extend "../../../services/chef"
    name "mysql-vanilla"
    type "DATABASE"
    icon "mysql.png"
    numInstances 1

    compute {
        template "SMALL_UBUNTU"
    }

    lifecycle {
        startDetectionTimeoutSecs 240
        startDetection {
            ServiceUtils.isPortOccupied(System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"], 3306)
        }
    }


    customCommands ([
            /*
               This custom command enables users to invoke an SQL statement
               Usage :  invoke mysql query actionUser dbName query
               Example: invoke mysql query root myDbName "update users set city=\"NY\" where uid=15"
           */
            "query" : "mysql_query.groovy"
    ])




}
