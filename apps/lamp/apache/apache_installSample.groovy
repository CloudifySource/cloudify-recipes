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
import org.hyperic.sigar.OperatingSystem
import org.cloudifysource.dsl.context.ServiceContextFactory

new GroovyShell().evaluate(new File("apache_postInstall.groovy"))

context = ServiceContextFactory.getServiceContext()
config = new ConfigSlurper().parse(new File("apache-service.properties").toURL())

def dbHost = context.attributes.thisInstance["dbHost"]

builder = new AntBuilder()


samplePhpFileName="sample.php"
samplePhpFile = new File("${samplePhpFileName}")
phpText = samplePhpFile.text
println "apache_installSample.groovy: Replacing REPLACE_WITH_DB_HOST with ${dbHost} in ${samplePhpFileName}..."
phpText = phpText.replace("REPLACE_WITH_DB_HOST","${dbHost}")

println "apache_installSample.groovy: Replacing REPLACE_WITH_DB_USER with ${config.dbUser} in ${samplePhpFileName}..."
phpText = phpText.replace("REPLACE_WITH_DB_USER","${config.dbUser}")

println "apache_installSample.groovy: Replacing REPLACE_WITH_DB_PASSWORD with ${config.dbPassW} in ${samplePhpFileName}..."
phpText = phpText.replace("REPLACE_WITH_DB_PASSWORD","${config.dbPassW}")

println "apache_installSample.groovy: Replacing REPLACE_WITH_DB_NAME with ${config.dbName} in ${samplePhpFileName}..."
phpText = phpText.replace("REPLACE_WITH_DB_NAME","${config.dbName}")

println "apache_installSample.groovy: Replacing REPLACE_WITH_TABLE_NAME with ${config.dbTableName} in ${samplePhpFileName}..."
phpText = phpText.replace("REPLACE_WITH_TABLE_NAME","${config.dbTableName}")
samplePhpFile.text =  phpText

docRoot = context.attributes.thisInstance["docRoot"]
println "apache_installSample.groovy: Using docRoot ${docRoot}"

builder.sequential {
	echo(message:"apache_installSample.groovy: Copying ${samplePhpFileName} to ${docRoot}/ ...")
	copy(todir: "${docRoot}", file:"${samplePhpFileName}", overwrite:true)	
}

println "apache_installSample.groovy: Ended"