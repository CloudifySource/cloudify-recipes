service {
  name "feeder"
  numInstances 1
  maxAllowedInstances 1
  statelessProcessingUnit {	
    binaries "rt-analytics-feeder.jar"    
    sla {
      highlyAvailable false
      memoryCapacityPerContainer 8 
    }
  }	
}