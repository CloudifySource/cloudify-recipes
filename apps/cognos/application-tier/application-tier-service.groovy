/*******************************************************************************
* Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
service {
        extend "../cognos"
        name "application-tier" 
        type "APP_SERVER"
        elastic false
		numInstances 2
        
        def currPort = 9300
        
        lifecycle{                      
                preStart "application-tier_preStart.groovy" 
                                
                startDetection {
                        println "application-tier-service.groovy(startDetection): checking port ${currPort} ..."
                        ServiceUtils.isPortOccupied(currPort)
                }

                stopDetection { 
                        !ServiceUtils.isPortOccupied(currPort)
                }   
                
        } 
}

