//import java.net.InetAddress 

service {
    extend "../../../services/chef"
    name "mysql-vanilla"
    type "DATABASE"
    numInstances 1

    compute {
        template "MEDIUM_UBUNTU"
    }

    lifecycle {
        startDetectionTimeoutSecs 240
        startDetection {
            ServiceUtils.isPortOccupied(System.getenv()["CLOUDIFY_AGENT_ENV_PRIVATE_IP"], 3306)
        }
    }

}
