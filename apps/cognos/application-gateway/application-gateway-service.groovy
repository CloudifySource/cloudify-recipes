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
	name "application-gateway"	
        type "WEB_SERVER"
        elastic false   
        
        def currPort = 80
 
        lifecycle{   

                details {
                        def currPublicIP = context.publicAddress                                                
                        def applicationURL = "http://${currPublicIP}:${currPort}/ibmcognos"
                        println "application-gateway-service.groovy: applicationURL is ${applicationURL}"                       
                        return [
                                "Application URL":"<a href=\"${applicationURL}\" target=\"_blank\">${applicationURL}</a>"
                        ]
                }
        
                preStart "application-gateway_preStart.groovy"
                start   ""
                                
                startDetection {
                        println "application-gateway-service.groovy(startDetection): checking port ${currPort} ..."
                        ServiceUtils.isPortOccupied(currPort)
                }

                stopDetection { 
                        !ServiceUtils.isPortOccupied(currPort)
                }               
        } 
}

