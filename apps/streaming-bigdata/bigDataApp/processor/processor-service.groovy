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
  }	
}