import java.util.concurrent.TimeUnit

service {
    name "processor"
    numInstances 2
    maxAllowedInstances 4
    statefulProcessingUnit {
        binaries "rt-analytics-processor.jar"
        sla {
            memoryCapacity 32
            maxMemoryCapacity 64
            highlyAvailable true
            memoryCapacityPerContainer 16
        }

        def cassandraService = context.waitForService("rt_cassandra", 180, TimeUnit.SECONDS)
        def cassandraInstances = cassandraService?.waitForInstances(dbService.numberOfPlannedInstances, 180, TimeUnit.SECONDS)
        def cassandraHost = cassandraInstances?.size() != 0 ? cassandraInstances[0].hostAddress : "localhost"
        println cassandraHost

        contextProperties [
            "cassandra.host": cassandraHost
        ]
    }
}