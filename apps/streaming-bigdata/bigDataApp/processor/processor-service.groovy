import java.util.concurrent.TimeUnit

service {
    name "processor"
    numInstances 4
    maxAllowedInstances 4
    statefulProcessingUnit {
        binaries "rt-analytics-processor.jar"
        sla {
            memoryCapacity 512
            maxMemoryCapacity 512
            highlyAvailable true
            memoryCapacityPerContainer 128
        }

    }
}