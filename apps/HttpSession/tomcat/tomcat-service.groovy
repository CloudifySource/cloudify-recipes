/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
	extend "../../../services/tomcat"
	
	numInstances 2
	minAllowedInstances 2
	maxAllowedInstances 3
	
	lifecycle {
		postInstall {
			def home = context.attributes.thisInstance["catalinaHome"]
			def installDir = System.properties["user.home"]+ "/.cloudify/${serviceName}" + context.instanceId
			def applicationWar = "${installDir}/${warName? warName : new File(applicationWarUrl).name}"
			builder = new AntBuilder()
			 def proc = "mv ${context.serviceDirectory}/HttpSession ${applicationWar}".execute()
			 proc.waitFor()
			def iniFile = new File("${applicationWar}/WEB-INF/shiro.ini") 
			def iniFileText = iniFile.text	
			def replacementStr = "%GS_LOCATOR%"
			def newStr = "" + context.attributes.thisApplication["locators"]
			iniFileText = iniFileText.replace(replacementStr,newStr) 
			iniFile.write(iniFileText)
/*
			iniFile = new File("${applicationWar}/WEB-INF/web.xml") 
			iniFileText = iniFile.text	
			replacementStr = "%JVMROUTE%"
			newStr = ".jvm" + context.instanceId
			iniFileText = iniFileText.replace(replacementStr,newStr) 
			iniFile.write(iniFileText)		
			*/
		}	
	}
}