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
import org.cloudifysource.dsl.utils.ServiceUtils;
import org.cloudifysource.dsl.context.ServiceContextFactory

def context = ServiceContextFactory.getServiceContext()

def config = new ConfigSlurper().parse(new File("tomcat-service.properties").toURL())


    def home = context.attributes.thisInstance["catalinaHome"]
    def installDir = System.properties["user.home"]+ "/.cloudify/${config.serviceName}" + context.instanceId
    def applicationWar = "${installDir}/${config.warName? config.warName : new File(config.applicationWarUrl).name}"
    builder = new AntBuilder()
     def proc = "mv ${context.serviceDirectory}/HttpSession ${applicationWar}".execute()
     proc.waitFor()
    def iniFile = new File("${applicationWar}/WEB-INF/shiro.ini")
    def iniFileText = iniFile.text
    def replacementStr = "%SPACE_URL%"

    def newStr = "" + context.attributes.thisApplication["SPACE_URL"]
    if( newStr.length() < 6){
        newStr = "" + context.attributes.global["SPACE_URL"]
    }
    else if( newStr.length() < 6) {
        newStr = "jini://*/*/cloudifyManagementSpace?locators="+ System.getenv('LOOKUPLOCATORS')
    }    
    println("Using space URL " + newStr + " as the space for the Tomcat session managment")
    iniFileText = iniFileText.replace(replacementStr,newStr)
    iniFile.write(iniFileText)
    println("before AntBuilder().sequential")
    new AntBuilder().sequential{
            echo(message:"Getting ${config.HttpSessionClassesAndJarPath} to ${installDir}/${config.HttpSessionClassesAndJarZipName}")
            get(src:"${config.HttpSessionClassesAndJarPath}", dest:"${installDir}/${config.HttpSessionClassesAndJarZipName}", skipexisting:true)
            echo(message:"Unzipping ${installDir}/${config.HttpSessionClassesAndJarZipName} to ${applicationWar}/WEB-INF/")
            unzip(src:"${installDir}/${config.HttpSessionClassesAndJarZipName}", dest:"${applicationWar}/WEB-INF/", overwrite:true)
    }
    println("after AntBuilder().sequential")

