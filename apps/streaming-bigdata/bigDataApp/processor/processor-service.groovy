service {
  name "processor"
  numInstances 2
  maxAllowedInstances 2
  statefulProcessingUnit {
    binaries "rt-analytics-processor.jar"   		
    sla {
      memoryCapacity 32
      maxMemoryCapacity 32
      highlyAvailable true
      memoryCapacityPerContainer 16 
    }
  }	
}