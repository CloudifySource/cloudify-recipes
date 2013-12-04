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
	name "content-database"	
        type "DATABASE"
        elastic false
        
        def currPort = 9300
        
        lifecycle{    

                details {
                        def currPublicIP = context.publicAddress                                                
                        def applicationURL = "http://${currPublicIP}:${currPort}/p2pd/servlet"
                        println "content-manager-service.groovy: applicationURL is ${applicationURL}"                   
                        return [
                                "Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
                        ]
                }
				
                install "${context.serviceDirectory}/content-manager_install.sh ${context.serviceDirectory}"
                preStart "content-manager_preStart.groovy" 
				                               
                startDetection {
                        println "content-manager-service.groovy(startDetection): checking port ${currPort} ..."
                        ServiceUtils.isPortOccupied(currPort)
                }

                stopDetection { 
                        !ServiceUtils.isPortOccupied(currPort)
                }               
        } 
}

